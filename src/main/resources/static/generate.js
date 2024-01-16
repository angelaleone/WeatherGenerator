window.onload = function () {
    // Get a reference to the list element
    var songCardContainer = document.getElementById("songCardContainer");
    var songCard = document.getElementById("songCardTemplate");
  
    var recsJsonData = JSON.parse(
      document.getElementById("recsJson").getAttribute("data-json")
    );
    console.log(recsJsonData);
  
    var songNames = [];
    var artistNames = [];
    var trackURI = [];
  
    //use callback function
    recsJsonData.tracks.forEach(function (tracks) {
      songNames.push(tracks.name);
    });
  
    recsJsonData.tracks.forEach(function (tracks) {
      var artists = tracks.artists.map(function (artists) {
        return artists.name;
      });
      var artistNamesString = artists.join(", ");
      artistNames.push(artistNamesString);
    });
  
    console.log(songNames);
    console.log(artistNames);
  
    //add song card to html
    var append = [];
    var songCardTemplate = document.querySelector("#songCardTemplate");
    var songCardContainer = document.querySelector("#songCardContainer");
  
    for (var i = 0; i < songNames.length; i++) {
      var divString =
        '<div class="col-sm-9"><h5 class="mb-1">' +
        songNames[i] +
        '</h5><p class="mb-1">' +
        artistNames[i] +
        "</p></div>";
      var li = document.createElement("li");
      li.classList.add("list-group-item", "songcard");
      li.innerHTML = '<div class="row">' + divString + "</div>";
      append.push(li);
    }
  
    for (var i = 0; i < append.length; i++) {
      songCardContainer.append(append[i]);
    }

   


// function regenerate1(locationData) {
//   //const locationData = JSON.parse(stringData);
//   const options = {
//     method: "POST",
//     headers: {
//       "Content-Type": "application/json"
//     },
//     body: JSON.stringify(locationData)
//   };
//   fetch("/generate", options);
//}
// const button = document.getElementById("regenerate");
// button.addEventListener("click", regenerate1);


  };