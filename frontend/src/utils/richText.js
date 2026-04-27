function escapeHtml(value) {
  return String(value ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function inlineFormat(text) {
  return escapeHtml(text)
    .replace(/\*\*(.+?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.+?)\*/g, "<em>$1</em>")
    .replace(/`(.+?)`/g, "<code>$1</code>");
}

export function renderAssistantContent(content) {
  const lines = String(content ?? "").replace(/\r/g, "").split("\n");
  const parts = [];
  let paragraph = [];
  let bulletItems = [];
  let orderedItems = [];

  const flushParagraph = () => {
    if (!paragraph.length) return;
    parts.push(`<p>${inlineFormat(paragraph.join("<br />"))}</p>`);
    paragraph = [];
  };

  const flushBullets = () => {
    if (!bulletItems.length) return;
    parts.push(`<ul>${bulletItems.map((item) => `<li>${inlineFormat(item)}</li>`).join("")}</ul>`);
    bulletItems = [];
  };

  const flushOrdered = () => {
    if (!orderedItems.length) return;
    parts.push(`<ol>${orderedItems.map((item) => `<li>${inlineFormat(item)}</li>`).join("")}</ol>`);
    orderedItems = [];
  };

  for (const rawLine of lines) {
    const line = rawLine.trim();

    if (!line) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      continue;
    }

    const heading = line.match(/^(#{1,6})\s+(.+)$/);
    if (heading) {
      flushParagraph();
      flushBullets();
      flushOrdered();
      const level = Math.min(3, heading[1].length + 1);
      parts.push(`<h${level}>${inlineFormat(heading[2])}</h${level}>`);
      continue;
    }

    const bullet = line.match(/^[-*]\s+(.+)$/);
    if (bullet) {
      flushParagraph();
      flushOrdered();
      bulletItems.push(bullet[1]);
      continue;
    }

    const ordered = line.match(/^\d+\.\s+(.+)$/);
    if (ordered) {
      flushParagraph();
      flushBullets();
      orderedItems.push(ordered[1]);
      continue;
    }

    flushBullets();
    flushOrdered();
    paragraph.push(line);
  }

  flushParagraph();
  flushBullets();
  flushOrdered();

  return parts.join("") || "<p>暂无内容</p>";
}
