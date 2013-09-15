function populateIngredients() {
    $.get('/ingredients/', function (data) {
	$.each(data, function(i, e) {
	    var ingredient = $('<li/>')
		.append($('<a/>', {class: 'label label-success', href: e.url, title: e.name})
			.text(e.name));
	    $('#ingredients').append(ingredient);
	});
    });
}

function populateRecipes() {
    $.get('/recipes/', function (data) {
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name));
	    $('#letters').append(recipe);
	});
    });
}

$(document).ready(function () {
    populateIngredients();
    populateRecipes();
});
