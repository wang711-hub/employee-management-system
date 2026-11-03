const questionInput = document.getElementById('question');
const queryForm = document.getElementById('ai-query-form');
const askButton = document.getElementById('ask-button');
const queryStatus = document.getElementById('query-status');
const answerPanel = document.getElementById('answer-panel');
const answerText = document.getElementById('answer-text');
const answerSource = document.getElementById('answer-source');

document.querySelectorAll('.example-question').forEach((button) => {
  button.addEventListener('click', () => {
    questionInput.value = button.textContent.trim();
    questionInput.focus();
  });
});

queryForm.addEventListener('submit', async (event) => {
  event.preventDefault();
  const question = questionInput.value.trim();
  if (!question || question.length > 500) return;

  askButton.disabled = true;
  queryStatus.textContent = '回答を取得しています…';
  answerPanel.hidden = true;
  const csrfToken = document.querySelector('meta[name="_csrf"]').content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

  try {
    const response = await fetch('/api/ai/query', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
      body: JSON.stringify({ question })
    });
    const result = await response.json();
    answerText.textContent = result.answer || '回答を取得できませんでした。';
    answerSource.textContent = result.source === 'database' ? 'データベースの集計結果に基づく回答です。' : '';
    answerPanel.hidden = false;
    queryStatus.textContent = response.ok ? '' : 'リクエストを完了できませんでした。';
  } catch (error) {
    queryStatus.textContent = '通信に失敗しました。時間をおいて再度お試しください。';
  } finally {
    askButton.disabled = false;
  }
});
