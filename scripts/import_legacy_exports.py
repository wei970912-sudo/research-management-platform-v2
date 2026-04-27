from __future__ import annotations

from io import StringIO
import json
from pathlib import Path
import re
from urllib import request as urllib_request

import pandas as pd

ROOT = Path(__file__).resolve().parents[2]
EXPORT_DIR = ROOT / "ppt_work_20260414_ppt_template" / "input" / "references"
BASE_URL = "http://127.0.0.1:8080/api"


def post_json(path: str, payload: dict) -> None:
    req = urllib_request.Request(
        f"{BASE_URL}{path}",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib_request.urlopen(req, timeout=20) as resp:
        print(path, resp.status)


def post_json_response(path: str, payload: dict) -> dict:
    req = urllib_request.Request(
        f"{BASE_URL}{path}",
        data=json.dumps(payload).encode("utf-8"),
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    with urllib_request.urlopen(req, timeout=20) as resp:
        return json.loads(resp.read().decode("utf-8"))["data"]


def get_projects() -> list[dict]:
    with urllib_request.urlopen(f"{BASE_URL}/projects", timeout=20) as resp:
        body = json.loads(resp.read().decode("utf-8"))
    return body["data"]


def project_id_map() -> dict[str, str]:
    return {item["name"]: item["id"] for item in get_projects()}


def ensure_project(project_map: dict[str, str], project_name: str) -> str:
    project_name = project_name.strip()
    if project_name in project_map:
        return project_map[project_name]

    created = post_json_response(
        "/projects",
        {
            "name": project_name,
            "owner": "旧版导入",
            "status": "进行中",
            "currentStage": "旧版导入",
            "milestoneCompletionRate": 0,
            "description": "从旧版静态软件导出数据自动导入。",
        },
    )
    project_map[project_name] = created["id"]
    return created["id"]


def xls_tables() -> tuple[Path, Path]:
    files = [p for p in EXPORT_DIR.iterdir() if p.suffix.lower() == ".xls"]
    meeting = next(p for p in files if "会议记录" in p.name)
    contract = next(p for p in files if "合同金额管理" in p.name)
    return meeting, contract


def read_html_tables(path: Path) -> list[pd.DataFrame]:
    raw = path.read_bytes()
    for encoding in ("utf-8", "gb18030", "gbk"):
        try:
            text = raw.decode(encoding)
            return pd.read_html(StringIO(text))
        except Exception:
            continue
    raise UnicodeDecodeError("legacy-import", raw, 0, 1, f"无法识别文件编码: {path}")


def import_meetings(project_map: dict[str, str], path: Path) -> None:
    tables = read_html_tables(path)
    df = tables[0].fillna("")
    for _, row in df.iterrows():
        project_name = str(row.get("所属项目", "")).strip()
        if not project_name:
            continue

        project_id = ensure_project(project_map, project_name)
        location = str(row.get("会议地点/平台", "")).strip()
        attendees = str(row.get("参会人员", "")).strip()
        payload = {
            "projectId": project_id,
            "topic": str(row.get("会议主题", "")).strip(),
            "date": normalize_date(str(row.get("会议日期", ""))),
            "host": "旧版导入",
            "type": str(row.get("会议形式", "")).strip() or "线上",
            "location": location or "旧版导入",
            "attendees": attendees or "旧版导入",
            "summary": str(row.get("会议详细内容", "")).strip() or "旧版导入",
            "attachmentNames": normalize_attachment_names(str(row.get("附件", "")).strip()),
        }
        if payload["topic"] and payload["date"]:
            post_json("/meetings", payload)


def import_contracts(project_map: dict[str, str], path: Path) -> None:
    tables = read_html_tables(path)
    if len(tables) < 2:
        return

    df = tables[1].fillna("")
    for _, row in df.iterrows():
        project_name = str(row.get("所属课题", "")).strip()
        if not project_name:
            continue

        project_id = ensure_project(project_map, project_name)
        amount = to_float(row.get("金额", 0))
        payload = {
            "projectId": project_id,
            "name": str(row.get("实际合同名称", "")).strip(),
            "amount": amount,
            "status": str(row.get("签订状态", "")).strip() or "待签订",
            "taskName": str(row.get("对应任务书项", "")).strip() or "旧版导入任务",
            "taskBudget": amount,
            "signingUnit": str(row.get("签订单位", "")).strip() or "旧版导入",
            "signingDate": normalize_date(str(row.get("签订/预计签订时间", "")).strip()) or "2026-04-14",
            "deliveryDate": normalize_date(str(row.get("合同交付时间", "")).strip()) or "2026-04-14",
            "content": str(row.get("内容", "")).strip() or "旧版导入",
            "deliveryTerms": str(row.get("交付条件", "")).strip() or "旧版导入",
            "paymentTerms": str(row.get("付款条件", "")).strip() or "旧版导入",
        }
        if payload["name"]:
            post_json("/contracts", payload)


def normalize_date(value: str) -> str:
    value = value.strip()
    if not value:
        return ""
    match = re.search(r"(\d{4})[/-](\d{2})[/-](\d{2})", value)
    if not match:
        return ""
    return f"{match.group(1)}-{match.group(2)}-{match.group(3)}"


def normalize_attachment_names(value: str) -> str:
    return value.replace("无附件", "").replace("，", ",").strip(" ,")


def to_float(value: object) -> float:
    text = str(value).replace(",", "").strip()
    return float(text or 0)


def main() -> None:
    project_map = project_id_map()
    meeting_path, contract_path = xls_tables()
    import_meetings(project_map, meeting_path)
    import_contracts(project_map, contract_path)
    print("legacy import finished")


if __name__ == "__main__":
    main()
