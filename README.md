# Indigo

![Indigo Logo](https://raw.githubusercontent.com/pabmartine/indigo/develop/indigo-frontend/src/assets/images/icon.png)

**Indigo is a free and open-source web interface for your Calibre e-book library, designed for usability, speed, and a modern user experience.**

---

## About The Project

Calibre is a fantastic e-book management tool, but it lacks a modern, fast, and user-friendly web interface to access your library from anywhere. Indigo was born out of the need for such a tool, taking inspiration from projects like Calibre-Web but built from the ground up with a modern technology stack and a focus on performance and a clean UI.

Indigo provides a beautiful and intuitive interface to browse, search, and manage your e-book collection. It's designed to be fast, responsive, and easy to use on any device.

## Key Features

*   **Modern & Fast Interface:** A clean, intuitive, and responsive UI built with Angular and PrimeNG.
*   **Multi-user Support:** Create accounts for different users to access your library.
*   **Advanced Search:** Powerful search capabilities to quickly find any book in your library.
*   **Author Biographies:** Get more information about your favorite authors.
*   **Ratings & Reviews:** Rate and review your books.
*   **Book Recommendations:** Discover new books based on your library and ratings.
*   **Favorites:** Mark your favorite books and authors for quick access.
*   **Send to Device:** Easily send books to your e-reader devices.
*   **Notifications:** Get notified about user events and other activities.

## Technologies Used

Indigo is built with a modern and robust technology stack:

*   **Backend:**
    *   Java 21
    *   Spring Boot 3
    *   MongoDB
*   **Frontend:**
    *   Angular 16
    *   PrimeNG
*   **Containerization:**
    *   Docker
    *   Docker Compose

## Getting Started

### Prerequisites

*   [Docker](https://docs.docker.com/get-docker/)
*   [Docker Compose](https://docs.docker.com/compose/install/)
*   [Java 21](https://www.oracle.com/java/technologies/downloads/#java21) (for manual installation)
*   [Node.js](https://nodejs.org/en/download/) (for manual installation)

### Installation

#### Docker (Recommended)

This is the easiest and recommended way to get Indigo up and running.

1.  Clone the repository:
    ```sh
    git clone https://github.com/pabmartine/indigo.git
    ```
2.  Navigate to the project directory:
    ```sh
    cd indigo
    ```
3.  Run the application using Docker Compose:
    ```sh
    docker-compose -f docker-compose-dev.yml up -d
    ```
4.  Open your browser and navigate to `http://localhost:8080`.

#### Manual Installation

If you prefer to run the backend and frontend services manually, follow these steps:

**Backend:**

1.  Navigate to the `indigo-backend` directory.
2.  Import the project into your favorite IDE (e.g., IntelliJ IDEA, Eclipse).
3.  Build the project using Maven.
4.  Run the `IndigoApplication.java` class.

**Frontend:**

1.  Navigate to the `indigo-frontend` directory.
2.  Install the dependencies:
    ```sh
    npm install
    ```
3.  Run the development server:
    ```sh
    ng serve
    ```
4.  Open your browser and navigate to `http://localhost:4200`.

## Usage

Once the application is running, you can access it at `http://localhost:8080` (if using Docker) or `http://localhost:4200` (if running the frontend manually).

**Default Admin Credentials:**

*   **Username:** `admin`
*   **Password:** `padmin`

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

Please refer to the contribution guidelines for more information.

## License

Distributed under the MIT License. See `LICENSE` for more information.
### Consumo de memoria en NAS

Las portadas siguen almacenándose como miniaturas JPEG en Base64 en MongoDB.
La importación usa un trabajador por defecto (`BOOK_LIBRARY_IMPORT_WORKERS=1`,
máximo 8). Los metadatos de libros ya se procesan secuencialmente, con una lista
inicial de identificadores, sin cargar todas las portadas.

El bus de eventos usa `EVENTS_WORKERS=1` (máximo 8) y una cola limitada por
`EVENTS_QUEUE_CAPACITY=32`. Al llenarse, el publicador ejecuta la tarea, frenando
la producción sin descartar eventos. Esto puede alargar peticiones que publiquen
eventos; no establece un límite global de un único hilo para toda la aplicación.
Las búsquedas de libros similares recorren páginas de 100 resultados.

Para un NAS de 6 GB compartido con otros servicios, un punto de partida a validar
es limitar el contenedor backend a 1536 MiB y establecer esta variable de entorno:

```text
JAVA_TOOL_OPTIONS=-Xms256m -Xmx1024m -XX:ActiveProcessorCount=2 -Xlog:gc=info
```

El Dockerfile actual permite usar `JAVA_TOOL_OPTIONS` sin cambiar el entrypoint.
El heap no incluye toda la memoria de Java: hay que reservar margen para memoria
nativa, pilas y otros componentes. `ActiveProcessorCount` afecta al dimensionado
de pools de la JVM, no impone una cuota dura de CPU. Estos límites deben aplicarse
en la configuración real de Container Manager; no se aplican automáticamente.

En MongoDB, `--wiredTigerCacheSizeGB 0.5` puede servir de punto de partida, pero
solo limita su caché interna, no toda la RAM del proceso. Véanse la documentación
oficial de [Java 25](https://docs.oracle.com/en/java/javase/25/docs/specs/man/java.html)
y [WiredTiger](https://www.mongodb.com/docs/manual/core/wiredtiger/).

Validar con la misma carga antes y después: memoria de los contenedores, CPU,
actividad de swap, pausas de GC y libros/minuto. Comprobar si el reinicio coincide
con `OutOfMemoryError` en Java o `OOMKilled` en Docker. Los nombres de hilos G1 y
la cantidad de swap ocupada por sí solos no demuestran la causa del fallo ni la
versión exacta de Java. No se ha realizado una prueba de carga con 169.000 EPUBs.
