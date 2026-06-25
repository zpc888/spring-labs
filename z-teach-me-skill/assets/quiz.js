document.querySelectorAll('.quiz').forEach((quiz) => {
  const answer = quiz.dataset.answer;
  const feedback = quiz.querySelector('.quiz-feedback');
  const buttons = quiz.querySelectorAll('button[data-choice]');

  buttons.forEach((button) => {
    button.addEventListener('click', () => {
      const correct = button.dataset.choice === answer;
      const explanation = button.dataset.explanation || '';
      feedback.textContent = (correct ? 'Correct. ' : 'Not quite. ') + explanation;
      feedback.className = 'quiz-feedback ' + (correct ? 'good' : 'bad');
    });
  });
});
