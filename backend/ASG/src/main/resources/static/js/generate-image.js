/* ==========================================================================
   generate-image.js
   AI 이미지 생성
   ========================================================================== */

async function handleAiImageGenerate() {
  const btn = document.getElementById("btn-ai-generate");
  const status = document.getElementById("ai-status");
  const btnText = btn?.querySelector(".ai-btn-text");

  if (!btn || !status) {
    console.error("필수 DOM 요소를 찾을 수 없습니다.");
    return;
  }

  const menuName = document.getElementById("menuName")?.value?.trim();
  if (!menuName) {
    alert("메뉴/상품명을 먼저 입력해주세요!");
    return;
  }

  const extraInfo = document.getElementById("extraInfo")?.value?.trim() ?? "";


  btn.disabled = true;
  btn.classList.add("loading");
  btn.classList.remove("done", "error");
  if (btnText) btnText.textContent = "생성 중...";
  status.className = "ai-status";
  status.textContent = "AI가 이미지를 생성 중입니다... (약 10~30초)";

  try {
    const response = await fetch("/api/v1/ai/generate", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        keyword: menuName,
        description: extraInfo
      }),
    });

    let result = {};
    try {
      result = await response.json();
    } catch (e) {
      result = {};
    }

    if (!response.ok) {
      throw new Error(result.error || `서버 오류 (${response.status})`);
    }

    injectImagePreview(result.imageUrl);

    btn.classList.remove("loading");
    btn.classList.add("done");
    if (btnText) btnText.textContent = "✓ 생성 완료";
    status.className = "ai-status success";
    status.textContent = "이미지가 생성되어 본문에 삽입되었습니다."; 

  } catch (error) {
    console.error("AI 이미지 생성 실패:", error);
    btn.classList.remove("loading");
    btn.classList.add("error");
    if (btnText) btnText.textContent = "AI 이미지 생성";
    status.className = "ai-status error";
    status.textContent = "오류: " + error.message;

  } finally {
    setTimeout(() => {
      btn.disabled = false;
      btn.classList.remove("done", "loading", "error");
      if (btnText) btnText.textContent = "AI 이미지 생성";
    }, 3000);
  }
}

function injectImagePreview(imageUrl) {
  const imgPreviews = document.getElementById("aiImgPreviews");
  if (!imgPreviews) return;

  const existing = document.getElementById("ai-generated-preview");
  if (existing) existing.remove();

  const previewDiv = document.createElement("div");
  previewDiv.className = "preview-item";
  previewDiv.id = "ai-generated-preview";
  previewDiv.innerHTML = `
    <img src="${imageUrl}" alt="AI 생성 이미지">
    <button type="button" class="btn-remove-img" onclick="this.parentElement.remove()">✕</button>
    <input type="hidden" class="uploaded-img-url" name="uploadedImgUrls" value="${imageUrl}">
  `;

  imgPreviews.appendChild(previewDiv);
}