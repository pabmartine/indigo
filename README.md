# Indigo
Free web interface for the Calibre e-book library

# About
Indigo is a web application for the management of the Calibre book library focused on usability and speed.

# Why
[Calibre](https://calibre-ebook.com) is a great tool, but it lacks a web interface that allows you to consult your library from anywhere. Looking for software that would allow me to do this, I came across [Calibre-Web](https://github.com/janeczku/calibre-web/) from [janeczku](https://github.com/janeczku/), a magnificent tool that I recommend everyone to try it. Calibre-Web, was at first, more than enough for the aim of being able to consult my library from anywhere, but, li``ttle by little I realized that it did not fit my personal needs 100%, so using it as an example, I decided to build my own version from scratch.

# Technologies
Indigo is made up of two independent modules, the front module coded in Angular 10 using PrimeNG and the back module in Java 8 with Spring Boot.

# Main Features
- Fast, clean and clear interface
- Multiple users
- Advanced search
- Biography of authors
- Ratings
- Similar books recommendations
- Selection of favorite books and authors
- Sending books to Kindle devices
- User event notification

# How to install
**Backend**
1. Clone or download the backend repository
2. Unzip the file if applicable
3. Open your favorite IDE (Eclipse, Intellij, etc) and import the maven project.
4. Update and compile the project
5. Run the IndigoApplication.java class

**Frontend**
1. Clone or download the frontend repository
2. Unzip the file if applicable
3. Open your favorite IDE (Visual Code or whatever) and import the project.
4. Install the dependencies with `npm install`
5. Run with `ng serve`
6. Open http://localhost:4200 on your favourite browser and enjoy

**Default admin login**\
Username: *admin*\
Password: *padmin*
