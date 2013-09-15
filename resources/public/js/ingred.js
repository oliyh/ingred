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
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#letters').append(recipe);
	});
    });
}

function loadRecipe(url) {
    $.get(url, function (recipe) {
	$('#recipe-name').text(recipe.name);
	$('#recipe-prep-time').text(recipe['preparation-time']);
	$('#recipe-cooking-time').text(recipe['cooking-time']);
	$('#recipe-yield').text(recipe.yield);

	$('#recipe-ingredients').empty();
	$.each(recipe.ingredients, function(i, e) {
	    var ingredient = $('<li/>')
		.append(e.qty)
		.append($('<a/>', {class: 'label label-success', href: e.url, title: e.name})
			.text(e.name))
		.append(e.preparation);
	    $('#recipe-ingredients').append(ingredient);
	});

	$('#recipe-instructions').empty();
	$.each(recipe.instructions, function(i, e) {
	    var ingredient = $('<li/>').text(e)
	    $('#recipe-instructions').append(ingredient);
	});

	$('#recipe-panel').show();
    });
}

$(document).ready(function () {
    populateIngredients();
    populateRecipes();
});
