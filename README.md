<div align="center">
    <img src="./app/src/main/res/drawable/logo.png" alt="logo" width="15%">
    <h1>PizzaHunter</h1>
</div>

PizzaHunter is a simple Android app, made to learn the basics of Android development using Kotlin.

PizzaHunter is a WIP food delivery app, where you can place orders and share items in the restaurant's menu with your friends.

## Technologies

-   [Kotlin](https://kotlinlang.org/)
-   [Firebase](https://firebase.google.com/docs) (Firestore, Storage and Auth)
-   [Facebook for developers](https://developers.facebook.com/)
-   [Google Services](https://developers.google.com/identity/sign-in/android/sign-in)
-   [Retrofit](https://square.github.io/retrofit/)
-   [Material.io](https://material.io/)
-   [Picasso](https://square.github.io/picasso/)
-   [Custom Food API](https://gunter-food-api.herokuapp.com/)

## Features

### ✔️ Authentication

-   simple authentication using email and password
-   social login using Facebook and Google

### ✔️ Data persistence using database

-   Firebase (user information & profile pictures)
-   user can edit the profile information

### ✔️ Camera - taking a photo

-   using [CameraX](https://developer.android.com/training/camerax) to take a photo
-   saving the image to the gallery
-   uploading the image to the Firebase Storage
-   user can also load a picture from the gallery as profile picture

### ✔️ Bottom Tab Navigation

-   [Material.io bottom navigation](https://material.io/components/bottom-navigation)

### ✔️ Dynamic lists with search option

-   RecyclerView for the food list in the menu
-   search option, to filter or find specific items

### ✔️ Android Sharesheet

-   sharing basic information about a particular item in the menu
-   it can be shared using any available application on the device

### ✔️ Web services

-   custom food API

    -   [Food API hosted on Heroku](https://gunter-food-api.herokuapp.com/)
    -   [Github repo](https://github.com/DianaVasiliu/Food-API)

-   fetching the API using Retrofit
-   using the data in the RecyclerView

### ✔️ Video playback

-   play a presentation video in the home page
-   user can pause and unpause the video

### ✔️ UI for landscape mode

## Screenshots

### Menu

<div>
    <img src="./screenshots/Menu1.png" width="30%" />
    <img src="./screenshots/Search.png" width="30%" />
</div>

### Social Login

<div>
    <img src="./screenshots/GoogleLogin.jpg" width="30%" />
    <img src="./screenshots/FacebookLogin.png" width="30%" />
</div>

### Edit profile information

<div>
    <img src="./screenshots/EditProfile0.png" width="30%" />
    <img src="./screenshots/EditProfile1.png" width="30%" />
    <img src="./screenshots/EditProfile2.png" width="30%" />
</div>

### Edit profile picture

<img src="./screenshots/ChangePicture1.png" width="30%" />

1. Take photo using camera

<div>
    <img src="./screenshots/TakePicture.png" width="30%" />
    <img src="./screenshots/SavePicture.png" width="30%" />
    <img src="./screenshots/UploadedPicture.png" width="30%" />
</div>

2. Upload picture from gallery

<div>
    <img src="./screenshots/Gallery.png" width="30%" />
    <img src="./screenshots/UploadedFromGallery.png" width="30%" />
</div>

### Sharesheet

<div>
    <img src="./screenshots/ItemToShare.png" width="30%" />
    <img src="./screenshots/Sharesheet.png" width="30%" />
    <img src="./screenshots/ShareResult.jpeg" width="30%" />
</div>

### Video playback

<div>
    <img src="./screenshots/Video1.png" width="30%" />
    <img src="./screenshots/Video2.png" width="30%" />
</div>

## TODO

See [TODO list](TODO.md) for more features that should be added in this app.
