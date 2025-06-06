# Formula 1 Race Strategy Optimization Platform

This is a web-based platform developed as part of my senior thesis project at the American University in Bulgaria. It enables analysis, prediction, and optimization of Formula 1 race strategies using machine learning and simulation techniques.

---

## Overview

The platform provides a comprehensive tool for:
- Predicting pit stop strategies
- Modeling tire degradation
- Simulating race outcomes
- Comparing alternative strategies

It leverages historical F1 race data and a trained neural network (via DeepLearning4J) to generate dynamic race strategy insights.

---

##  Key Features

-  **Strategy Optimization:** Predict optimal tire and pit stop strategies based on driver and track characteristics.
-  **Lap Time & Tire Degradation Modeling:** Interactive graphs visualize lap times, tire wear, and compound usage.
-  **Custom Driver Creation:** Users can simulate scenarios using custom drivers with adjustable parameters.
-  **Race Simulation Engine:** Simulates race outcomes using real-world variables (weather, track temp, tire wear).
-  **Strategy Comparison:** Evaluate different strategies (e.g., one-stop vs. two-stop) based on predicted total race time.
-  **Responsive UI:** Built with HTML/CSS/JS (Bootstrap & Chart.js) for real-time interaction.

---

##  Technologies Used

- **Backend:** Java 17, Spring Boot, Spring Data JPA
- **Machine Learning:** DeepLearning4J (DL4J)
- **Frontend:** HTML5, CSS3, Bootstrap 5, JavaScript, Thymeleaf, Chart.js
- **Database:** MySQL (with Workbench), JPA (Hibernate)
- **Architecture:** MVC (Model-View-Controller)

---

##  Installation & Running

### Prerequisites
- Java 17+
- Maven
- MySQL
- Web browser (Chrome/Firefox recommended)

### Steps

#### 1. Clone the repository
```bash
git clone https://github.com/your-username/f1-strategy-platform.git
cd f1-strategy-platform
```

#### 2. Configure your MySQL database (create a database named `f1_strategy_db` and change the password in `application.properties`)

#### 3. Build the project
```bash
mvn clean install
```

#### 4. Run the application
```bash
mvn spring-boot:run
```

#### 5. Visit `http://localhost:8080` in your browser to access the platform.
