# Contributing to Cobalt

Thank you for your interest in contributing to **Cobalt**! Whether you're reporting a bug, suggesting a feature, or submitting a pull request, your contributions are invaluable to improving the project.

## How to Contribute

### Reporting Issues

If you encounter any bugs or have suggestions for improvements, please [open an issue](https://github.com/Kubik-Modder/cobalt-fabric/issues) with the following details:

1. **Description:** A clear and concise description of the problem or suggestion.
2. **Steps to Reproduce:** Detailed steps to help us understand and reproduce the issue.
3. **Expected Behavior:** What you expected to happen.
4. **Actual Behavior:** What actually happened.
5. **Screenshots:** If applicable, add screenshots to help explain your issue.
6. **Logs:** Include relevant logs or crash reports if applicable.

### Submitting Pull Requests

I welcome pull requests! To ensure a smooth review process, please follow these steps:

1. **Fork the Repository:** Click the "Fork" button at the top right of the repository page to create your own fork.
2. **Clone Your Fork:** Clone the forked repository to your local machine.
   ```bash
   git clone https://github.com/yourusername/cobalt-fabric.git
3. **Create a Branch:** Create a new branch for your feature or bugfix.
   ```bash
   git checkout -b feature/your-feature-name
4. **Make Your Changes:** Implement your feature or fix the bug following the code guidelines below.
5. **Commit Your Changes:** Commit your changes with clear and descriptive messages.
   ```bash
   git commit -m "Add feature X to improve Y"
6. **Push to Your Fork:** Push your changes to your forked repository.
   ```bash
   git push origin feature/your-feature-name
7. **Open a Pull Request:** Navigate to the original repository and click "Compare & pull request". Provide a clear description of your changes and reference any related issues.

## Code Guidelines
To maintain consistency and readability across the codebase, please adhere to the following guidelines:

### Indentation

* Use 4 spaces for indentation.
* Do not use tabs.

### Comments

* Use **Javadoc-style** comments for all classes, methods, and significant code blocks.
* Use **inline comments** sparingly to explain complex logic or important decisions.

### Naming Conventions

* **Classes and Interfaces:** Use `PascalCase`.
* **Methods and Variables:** Use `camelCase`.
* **Constants:** Use `UPPER_CASE_WITH_UNDERSCORES`.

### Formatting

* **Line Length:** Aim for a maximum of 100 characters per line for readability.
* **Braces:** Place opening braces on the same line as the declaration.

### Documentation

* **Update Javadoc:** Ensure that all Javadoc comments accurately describe the functionality and parameters of methods and classes.
* **README:** If your changes affect the usage or setup of Cobalt, update the `README.md` accordingly.

## Licensing

* Cobalt is licensed under the GNU General Public License v3.0.
* By contributing to this project, you agree that your contributions will be licensed under the same GNU GPL v3.0 license.