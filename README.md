# ◆ Taskflow — To-Do List Application

A modern, dark-themed desktop To-Do List application built with **Java Swing**, following a clean 3-layer architecture. Features a fully custom frameless window with animated background, maximize/restore support, and smooth UI interactions.

---

## 📸 Features

- ✅ Add, delete, and complete tasks
- ✅ Custom dark UI with animated floating orbs
- ✅ Frameless window with custom title bar
- ✅ Maximize / Restore / Minimize window controls
- ✅ Drag title bar to move the window
- ✅ Resizable window with drag grip (bottom-right corner)
- ✅ Double-click title bar to maximize/restore
- ✅ Double-click a task to mark it as done
- ✅ Shake animation on empty input
- ✅ Task counter with completed status
- ✅ Strikethrough + green "Done" badge for completed tasks
- ✅ Enter key support for adding tasks

---

## 🏗️ Project Structure

```
TodoAppMaven/
├── pom.xml
└── src/
    └── main/
        └── java/
            ├── Main.java               # Entry point
            ├── model/
            │   └── Task.java           # Task data model
            ├── service/
            │   └── TaskService.java    # Business logic (add, delete, complete)
            └── gui/
                └── TodoFrame.java      # Full Swing GUI
```

---

## ⚙️ Tech Stack

| Technology | Details |
|---|---|
| Language | Java 11+ |
| GUI Framework | Java Swing |
| Build Tool | Apache Maven |
| Architecture | 3-Layer (Model / Service / GUI) |
| Data Structure | ArrayList |

---

## 🚀 Getting Started

### Prerequisites

Make sure the following are installed on your system:

- [Java JDK 11+](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven](https://maven.apache.org/download.cgi)

Verify installations:
```bash
java -version
mvn -version
```

### Clone the Repository

```bash
git clone https://github.com/YourUsername/todo-list-app.git
cd todo-list-app
```

### Build the Project

```bash
mvn clean package
```

### Run the Application

```bash
java -jar target/todo-list-app.jar
```

---

## 🖥️ How to Use

| Action | How |
|---|---|
| **Add a task** | Type in the input field → click `＋ Add Task` or press `Enter` |
| **Mark as done** | Select a task → click `✔ Mark Done` or **double-click** the task |
| **Delete a task** | Select a task → click `✕ Delete` → confirm |
| **Maximize window** | Click the 🟢 green dot or **double-click** the title bar |
| **Minimize window** | Click the 🟡 yellow dot |
| **Close window** | Click the 🔴 red dot |
| **Move window** | Drag the title bar |
| **Resize window** | Drag the grip at the bottom-right corner |

---

## 📁 Architecture Overview

```
┌─────────────────────────────┐
│         GUI Layer           │  TodoFrame.java
│   (Swing, Event Handling)   │
└────────────┬────────────────┘
             │
┌────────────▼────────────────┐
│       Service Layer         │  TaskService.java
│     (Business Logic)        │
└────────────┬────────────────┘
             │
┌────────────▼────────────────┐
│        Model Layer          │  Task.java
│       (Data / State)        │
└─────────────────────────────┘
```

---

## 🔮 Future Enhancements

- [ ] File or database storage to persist tasks between sessions
- [ ] Task priority levels (High / Medium / Low)
- [ ] Due dates and deadline reminders
- [ ] Search and filter tasks
- [ ] Migrate UI to JavaFX for richer visuals
- [ ] Dark / Light theme toggle

---

## 📝 License

This project is open source and available under the [MIT License](LICENSE).

---

## 👨‍💻 Author

Built with ❤️ using Java Swing.  
Feel free to fork, contribute, or open issues!