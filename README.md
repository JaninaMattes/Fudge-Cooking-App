<div id="top"></div>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Contributors][contributors-shield]][contributors-url]
[![Forks][forks-shield]][forks-url]
[![Stargazers][stars-shield]][stars-url]
[![Issues][issues-shield]][issues-url]
[![MIT License][license-shield]][license-url]
[![LinkedIn][linkedin-shield]][linkedin-url]


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="assets/images/logo.png" alt="Logo" width="100%" height="100%">
  </a>

  <h3 align="center">Best-README-Template</h3>

  <p align="center">
    An awesome README template to jumpstart your projects!
    <br />
    <a href="https://github.com/othneildrew/Best-README-Template"><strong>Explore the docs »</strong></a>
    <br />
    <br />
    <a href="https://github.com/othneildrew/Best-README-Template">View Demo</a>
    ·
    <a href="https://github.com/othneildrew/Best-README-Template/issues">Report Bug</a>
    ·
    <a href="https://github.com/othneildrew/Best-README-Template/issues">Request Feature</a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table of Contents</summary>
  <ol>
    <li>
      <a href="#about-the-project">About The Project</a>
      <ul>
        <li><a href="#built-with">Built With</a></li>
      </ul>
    </li>
    <li>
      <a href="#getting-started">Getting Started</a>
      <ul>
        <li><a href="#prerequisites">Prerequisites</a></li>
        <li><a href="#installation">Installation</a></li>
      </ul>
    </li>
    <li><a href="#usage">Usage</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#contributing">Contributing</a></li>
    <li><a href="#license">License</a></li>
    <li><a href="#contact">Contact</a></li>
    <li><a href="#acknowledgments">Acknowledgments</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## About The Project
Fudge is a mobile-first Android application for smart pantry tracking, barcode-based ingredient scanning, and personalised recipe recommendations. 
Fudge serves as the foundation for the extended [Cookly](https://github.com/JaninaMattes/Smart-Pantry-Planner/). project.

This repository documents the design and development of **Fudge Recipe**, including the progression from an early-stage **interactive UX/UI prototype** created in Figma.
To help understand the overall user flow and screen structure, a **click-through Figma prototype** is available. It demonstrates the core navigation and layout of the application and can be explored in the browser.

### 🎨 Interactive Design Prototype

<div align="center">
  <img src="assets/images/figma.png" alt="Figma navigable screens preview" width="100%">
</div>

<div align="center">

[🔗 **Open the Interactive Figma Prototype**](https://www.figma.com/proto/J7pup60VT1Uw12lk1U4hGL/FeedMe-App?node-id=1-4&starting-point-node-id=1%3A4)

</div>

> **Note:** The prototype is intentionally simple and focuses on user flow and screen transitions rather than final visuals. You can click through the screens directly in your browser.

---

## About The Project

Fudge helps users manage their home inventory, scan product 
barcodes to log ingredients, and receive recipe suggestions 
based on what they already have. The app follows MVVM 
architecture with clean separation between UI, business logic 
and data layers.

<p align="right">(<a href="#top">back to top</a>)</p>



## Built With

**Frontend (Android)**
- Kotlin & Gradle
- MVVM Architecture (ViewModel + LiveData)
- Jetpack Navigation Component
- Hilt (Dagger) – dependency injection
- Retrofit + OkHttp – REST API communication
- Glide / Picasso – image loading
- ZXing – barcode scanning
- WorkManager – background tasks
- ViewBinding

**Backend**
- Java / Spring Boot
- JWT Authentication
- REST APIs

**Design**
- Figma – UI/UX prototyping

<p align="right">(<a href="#top">back to top</a>)</p>

<div align="center">
  <a href="https://github.com/othneildrew/Best-README-Template">
    <img src="assets/images/architecture.png" alt="Project Architecture" width="100%" height="100%">
  </a>
</div>

<!-- GETTING STARTED -->
## Getting Started

### Prerequisites
- Android Studio Bumblebee or later
- Android SDK 27+
- Java 11+

### Installation
1. Clone the repo
```sh
   git clone https://github.com/JaninaMattes/Smart-Pantry-Planner.git
```
2. Open in Android Studio
3. Add your API keys to `local.properties`
```
   SPOONACULAR_API_KEY=your_key_here
```
4. Run on emulator or physical device (API 27+)

<p align="right">(<a href="#top">back to top</a>)</p>

## Architecture

The app follows **MVVM** with a repository pattern:
- **UI Layer** – Fragments + ViewBinding
- **ViewModel Layer** – LiveData, Coroutines
- **Repository Layer** – Retrofit API calls, local caching
- **DI** – Hilt for dependency injection throughout
  

<!-- ROADMAP -->
## Roadmap

- [x] Add Changelog
- [x] Add back to top links
- [ ] Add Additional Templates w/ Examples
- [ ] Add "components" document to easily copy & paste sections of the readme
- [ ] Multi-language Support
    - [ ] Chinese
    - [ ] Spanish

See the [open issues](https://github.com/othneildrew/Best-README-Template/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the MIT License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Your Name - [@your_twitter](https://twitter.com/your_username) - email@example.com

Project Link: [https://github.com/your_username/repo_name](https://github.com/your_username/repo_name)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

Use this space to list resources you find helpful and would like to give credit to. I've included a few of my favorites to kick things off!

* [Choose an Open Source License](https://choosealicense.com)
* [GitHub Emoji Cheat Sheet](https://www.webpagefx.com/tools/emoji-cheat-sheet)
* [Malven's Flexbox Cheatsheet](https://flexbox.malven.co/)
* [Malven's Grid Cheatsheet](https://grid.malven.co/)
* [Img Shields](https://shields.io)
* [GitHub Pages](https://pages.github.com)
* [Font Awesome](https://fontawesome.com)
* [React Icons](https://react-icons.github.io/react-icons/search)

<p align="right">(<a href="#top">back to top</a>)</p>



<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[contributors-shield]: https://img.shields.io/github/contributors/othneildrew/Best-README-Template.svg?style=for-the-badge
[contributors-url]: https://github.com/othneildrew/Best-README-Template/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/othneildrew/Best-README-Template.svg?style=for-the-badge
[forks-url]: https://github.com/othneildrew/Best-README-Template/network/members
[stars-shield]: https://img.shields.io/github/stars/othneildrew/Best-README-Template.svg?style=for-the-badge
[stars-url]: https://github.com/othneildrew/Best-README-Template/stargazers
[issues-shield]: https://img.shields.io/github/issues/othneildrew/Best-README-Template.svg?style=for-the-badge
[issues-url]: https://github.com/othneildrew/Best-README-Template/issues
[license-shield]: https://img.shields.io/github/license/othneildrew/Best-README-Template.svg?style=for-the-badge
[license-url]: https://github.com/othneildrew/Best-README-Template/blob/master/LICENSE.txt
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://linkedin.com/in/othneildrew
[product-screenshot]: assets/images/screenshot.png
