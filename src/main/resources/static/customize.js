//for customize
document.addEventListener('DOMContentLoaded', () => {
  
    document.querySelector('form').addEventListener('submit', function(event) {
      event.preventDefault();
      
      // Create an array of selected genres
      const genres = [];
      const checkboxes = document.querySelectorAll('input[type="checkbox"]');
      checkboxes.forEach((checkbox) => {
        if (checkbox.checked) {
          genres.push(checkbox.value);
        }
      });
      
      // Modify the form data to include the genres array
      var formData = new FormData(this);
      formData.append('genres', JSON.stringify(genres));
    
      // Send the form data to the server
      fetch("/preferences", {
          method: 'POST',
          body: formData
      })
      .then(function(response) {
          if (!response.ok) {
              throw Error(response.statusText);
          }
          window.location.replace("/index");
          return response.text();
      })
      .catch(function(error) {
          console.error(error);
      });
    });
    });