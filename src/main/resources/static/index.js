//get the user's current location
function geolocate() {
  if (navigator.geolocation) {
    var locationInput = document.getElementById("location-input");
    navigator.geolocation.getCurrentPosition(function (position) {
      var latitude = position.coords.latitude;
      var longitude = position.coords.longitude;
      locationInput.value = latitude + ", " + longitude;
    });
  } else {
    locationInput.value = "Geolocation is not supported by this browser.";
  }
}


//making the playlist using song cards

//get the recs json using thymeleaf's model attribute











