# Lucky Wheel
[![Lint debug](https://github.com/joshanjohn/luckywheel_3092883/actions/workflows/luckywheel_lint_ci.yml/badge.svg)](https://github.com/joshanjohn/luckywheel_3092883/actions/workflows/luckywheel_lint_ci.yml)


Lucky Wheel is a gaming app. The main goal of the app is to have a compe<<ve gameplay for
collec<ng the gold by spinning a wheel using a mobile sensor . The spinning wheel will land the
pointer on a random choice (arch) on the spinning wheel. The player can earn or lose gold by
spinning the lucky wheel. The app shows Real-<me data ranking for players with high gold
count.

<img width="772" height="521" alt="image" src="https://github.com/user-attachments/assets/bcb9c07a-d910-4a48-8838-1015d82faa78" />



## Project Architecture
The project is developed in an agile SDCL method. The project uses the Model View Controller (MVC) 3-level design architecture paTern. All the model classes, defined as data classes in Kotlin, are organized within the data package.The services package contains all service and controller-related code, including the Firebase service, authen<ca<on service, and data store service. The UI elements, such as themes, screens, components, and UI logic, are placed in the UI package, along with the themes.


## 📚 SetUp Documentation
For detailed setup instructions, see the [Project Setup Guide](docs/PROJECT_SETUP.md).


## Dependency Security Management

This repository uses **[Mend Bolt](https://www.mend.io/free-developer-tools/bolt/)** for automated dependency security scanning and vulnerability management.

### What Mend Bolt Does
- 🔍 **Automatic Scanning**: Scans all dependencies on every pull request
- 🛡️ **CVE Detection**: Identifies known security vulnerabilities (CVEs) in dependencies
- 🎯 **Severity Filtering**: Reports MEDIUM, HIGH, and CRITICAL vulnerabilities
- 📋 **Auto Issue Creation**: Automatically creates GitHub issues for detected vulnerabilities
- ✅ **PR Blocking**: Prevents merging pull requests with critical security issues
- 📜 **License Compliance**: Checks for license violations and incompatibilities

### Security Configuration
- **Minimum Severity**: MEDIUM and above
- **Strict Mode**: Enabled (blocks PRs with vulnerabilities)
- **Base Branch**: `main`
- **Issue Type**: Dependency vulnerabilities

All security findings are automatically tracked in the [Issues](../../issues?q=is%3Aissue+label%3Asecurity) tab with the `security` label.

## Colour Theme
The App uses the following colours for the app scaﬀold background colour, in a linear gradient
fashion. For some elements, it has a golden yellow colour.


 ## Project Development 
 - Milestone 1 - https://github.com/joshanjohn/luckywheel_3092883/blob/main/docs/Joshan_John_3092883_milestone1.pdf
 - Milestone 2 - https://github.com/joshanjohn/luckywheel_3092883/blob/milestone-3/docs/milestone%202/Joshan_John_3092883_milestone2.pdf
 - Milestone 3 - in progress

