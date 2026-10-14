const nameInput = document.querySelector("#name-input");
const saveNameButton = document.querySelector("#save-name");
const profileName = document.querySelector("#profile-name");
const topicGrid = document.querySelector("#topic-grid");
const quizArea = document.querySelector("#quiz-area");
const historyList = document.querySelector("#history-list");
const attemptCount = document.querySelector("#attempt-count");
const topicIcons = ["⌘", "◈"];

let studentName = localStorage.getItem("prepwise-name") || "";
nameInput.value = studentName;
updateProfile();
loadTopics();
loadHistory();

saveNameButton.addEventListener("click", () => {
  const name = nameInput.value.trim();
  if (!name || name.length > 60) {
    nameInput.setCustomValidity("Enter a name between 1 and 60 characters.");
    nameInput.reportValidity();
    return;
  }
  nameInput.setCustomValidity("");
  studentName = name;
  localStorage.setItem("prepwise-name", studentName);
  updateProfile();
  loadHistory();
});

nameInput.addEventListener("input", () => nameInput.setCustomValidity(""));

function updateProfile() {
  profileName.textContent = studentName || "Guest";
}

async function request(url, options) {
  const response = await fetch(url, options);
  const data = await response.json();
  if (!response.ok) throw new Error(data.error || "Something went wrong.");
  return data;
}

async function loadTopics() {
  try {
    const data = await request("/api/topics");
    topicGrid.replaceChildren(...data.topics.map((topic, index) => {
      const card = document.createElement("article");
      card.className = "topic-card";
      const top = document.createElement("div");
      top.className = "topic-top";
      const icon = document.createElement("span");
      icon.className = "topic-icon";
      icon.textContent = topicIcons[index % topicIcons.length];
      const count = document.createElement("span");
      count.className = "question-count";
      count.textContent = `${topic.questionCount} QUESTIONS`;
      top.append(icon, count);

      const title = document.createElement("h3");
      title.textContent = topic.name;
      const description = document.createElement("p");
      description.textContent = topicDescription(topic.name);
      const action = document.createElement("button");
      action.className = "card-action text-button";
      action.type = "button";
      action.innerHTML = "Start practicing <span aria-hidden=\"true\">↗</span>";
      action.addEventListener("click", () => startQuiz(topic.name));
      card.append(top, title, description, action);
      return card;
    }));
  } catch (error) {
    topicGrid.replaceChildren(messageElement(error.message, "error-message"));
  }
}

function topicDescription(topic) {
  if (topic === "Core Java") return "Practice Java syntax, strings, exceptions, collections, and language fundamentals.";
  return "Explore encapsulation, abstraction, inheritance, polymorphism, and object relationships.";
}

async function startQuiz(topic) {
  quizArea.hidden = false;
  quizArea.replaceChildren(messageElement("Loading your quiz…", "muted"));
  quizArea.scrollIntoView({ behavior: "smooth", block: "start" });
  try {
    const data = await request(`/api/quiz?topic=${encodeURIComponent(topic)}`);
    renderQuiz(data);
  } catch (error) {
    quizArea.replaceChildren(messageElement(error.message, "error-message"));
  }
}

function renderQuiz(quiz) {
  const header = document.createElement("div");
  header.className = "quiz-top";
  const headingGroup = document.createElement("div");
  const kicker = document.createElement("div");
  kicker.className = "section-kicker";
  kicker.textContent = "PRACTICE SESSION";
  const title = document.createElement("h2");
  title.textContent = quiz.topic;
  headingGroup.append(kicker, title);
  const close = document.createElement("button");
  close.className = "text-button";
  close.type = "button";
  close.textContent = "Close ×";
  close.addEventListener("click", () => { quizArea.hidden = true; });
  header.append(headingGroup, close);

  const form = document.createElement("form");
  const questionList = document.createElement("div");
  questionList.className = "question-list";
  quiz.questions.forEach((question, index) => {
    const card = document.createElement("fieldset");
    card.className = "question-card";
    const legend = document.createElement("legend");
    legend.className = "question-title";
    const number = document.createElement("span");
    number.className = "question-number";
    number.textContent = `Q${String(index + 1).padStart(2, "0")}`;
    legend.append(number, document.createTextNode(question.prompt));
    const options = document.createElement("div");
    options.className = "options";
    question.options.forEach((option, optionIndex) => {
      const label = document.createElement("label");
      label.className = "option";
      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = `question-${index}`;
      radio.value = String(optionIndex + 1);
      radio.required = true;
      const text = document.createElement("span");
      text.textContent = option;
      label.append(radio, text);
      options.append(label);
    });
    card.append(legend, options);
    questionList.append(card);
  });

  const error = document.createElement("p");
  error.className = "error-message";
  error.hidden = true;
  const actions = document.createElement("div");
  actions.className = "quiz-actions";
  const hint = document.createElement("span");
  hint.className = "quiz-hint";
  hint.textContent = "Choose one answer for each question.";
  const submit = document.createElement("button");
  submit.className = "button button-dark";
  submit.type = "submit";
  submit.textContent = "Submit answers";
  actions.append(hint, submit);
  form.append(questionList, error, actions);
  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    if (!studentName) {
      error.textContent = "Save your name above before submitting your quiz.";
      error.hidden = false;
      document.querySelector(".welcome-row").scrollIntoView({ behavior: "smooth" });
      nameInput.focus();
      return;
    }
    const answers = quiz.questions.map((_, index) =>
      form.querySelector(`input[name="question-${index}"]:checked`)?.value);
    if (answers.some((answer) => !answer)) {
      error.textContent = "Please answer every question before submitting.";
      error.hidden = false;
      return;
    }
    submit.disabled = true;
    submit.textContent = "Checking answers…";
    try {
      const body = new URLSearchParams({ name: studentName, topic: quiz.topic, answers: answers.join(",") });
      const result = await request("/api/submit", {
        method: "POST",
        headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
        body
      });
      renderResult(result);
      loadHistory();
    } catch (submitError) {
      error.textContent = submitError.message;
      error.hidden = false;
      submit.disabled = false;
      submit.textContent = "Submit answers";
    }
  });
  quizArea.replaceChildren(header, form);
}

function renderResult(data) {
  const header = document.createElement("div");
  header.className = "quiz-top";
  const title = document.createElement("h2");
  title.textContent = "Your results";
  const close = document.createElement("button");
  close.className = "text-button";
  close.type = "button";
  close.textContent = "Close ×";
  close.addEventListener("click", () => { quizArea.hidden = true; });
  header.append(title, close);

  const banner = document.createElement("div");
  banner.className = "result-banner";
  const score = document.createElement("div");
  score.className = "result-score";
  score.textContent = `${data.result.score} / ${data.result.total} `;
  const small = document.createElement("small");
  small.textContent = `correct · ${data.result.percentage}%`;
  score.append(small);
  const topic = document.createElement("span");
  topic.className = "score-chip";
  topic.textContent = data.result.topic;
  banner.append(score, topic);

  const review = data.review.map((item) => {
    const card = document.createElement("article");
    card.className = "review-card";
    const status = document.createElement("div");
    status.className = `review-status ${item.isCorrect ? "correct" : "incorrect"}`;
    status.textContent = item.isCorrect ? "✓ Correct" : "✕ Not quite";
    const prompt = document.createElement("div");
    prompt.textContent = item.prompt;
    const answer = document.createElement("p");
    answer.textContent = `Your answer: ${item.selected} · Correct answer: ${item.correct}`;
    const explanation = document.createElement("p");
    explanation.textContent = item.explanation;
    card.append(status, prompt, answer, explanation);
    return card;
  });
  quizArea.replaceChildren(header, banner, ...review);
}

async function loadHistory() {
  if (!studentName) {
    attemptCount.textContent = "0 attempts";
    historyList.replaceChildren(messageElement("Save your name to see your quiz history here.", "muted"));
    return;
  }
  try {
    const data = await request(`/api/history?name=${encodeURIComponent(studentName)}`);
    const results = [...data.results].reverse();
    attemptCount.textContent = `${results.length} ${results.length === 1 ? "attempt" : "attempts"}`;
    if (results.length === 0) {
      historyList.replaceChildren(messageElement("No quizzes yet. Pick a topic above to get started.", "muted"));
      return;
    }
    historyList.replaceChildren(...results.map((result) => {
      const row = document.createElement("div");
      row.className = "history-row";
      const topic = document.createElement("span");
      topic.className = "history-topic";
      topic.textContent = result.topic;
      const score = document.createElement("span");
      score.className = "history-meta";
      score.textContent = `${result.score} of ${result.total} correct`;
      const date = document.createElement("span");
      date.className = "history-meta";
      date.textContent = result.completedAt;
      const percentage = document.createElement("span");
      percentage.className = "score-chip";
      percentage.textContent = `${result.percentage}%`;
      row.append(topic, score, date, percentage);
      return row;
    }));
  } catch (error) {
    historyList.replaceChildren(messageElement(error.message, "error-message"));
  }
}

function messageElement(text, className) {
  const message = document.createElement("p");
  message.className = className;
  message.textContent = text;
  return message;
}
