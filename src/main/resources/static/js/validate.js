var coll = document.getElementsByClassName("collapsible");
var i;

for (i = 0; i < coll.length; i++) {
    coll[i].addEventListener("click", function () {
        this.classList.toggle("active");
        var content = this.nextElementSibling;
        if (content.style.display === "block") {
            content.style.display = "none";
        } else {
            content.style.display = "block";
        }
    });
}

function validateURL() {
    var a = document.getElementById('webidURL').value;
    if (a.toString().includes("https://")) {
        return true;
    } else {
        document.getElementById('url_ok').style.display = "block"
        return false;
    }
}
// window.addEventListener("load", function(){
//     // Dummy Array
//     var data2 = $("#result").val();
//     var data = ["doge", "cate", "birb", "doggo", "moon moon", "awkward seal"];
//
//     // Draw HTML table
//     var perrow = 3, // 3 cells per row
//         count = 0, // Flag for current cell
//         table = document.createElement("table"),
//         row = table.insertRow();
//
//     for (var i of data) {
//         var cell = row.insertCell();
//         cell.innerHTML = i;
//
//         /* You can also attach a click listener if you want
//         cell.addEventListener("click", function(){
//           alert("FOO!");
//         });
//         */
//
//         // Break into next row
//         count++;
//         if (count%perrow==0) {
//             row = table.insertRow();
//         }
//     }
//
//     // Attach table to container
//     document.getElementById("container").appendChild(table);
// });