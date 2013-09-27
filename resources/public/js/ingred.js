function populateIngredients() {
    $.get('/ingredients/', function (data) {
	$('#ingredients').empty();
	$.each(data, function(i, e) {
	    var ingredient = $('<li/>')
		.append($('<a/>', {class: 'label label-success', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { filterRecipes(e.name); return false; }));
	    $('#ingredients').append(ingredient);
	});
    });
}

function filterRecipes(ingredient) {
    $.get('/ingredients/' + ingredient, function (data) {
	$('#letters').empty();
	$('#filter-ingredient').text(ingredient);
	$('#filter').show();
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

function resetFilter() {
    populateRecipes();
}


// admin

function populateRecipes() {
    $.get('/recipes/', function (data) {
	$('#filter').hide();
	$('#letters').empty();
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#letters').append(recipe);
	});
    });
}

function wipeStore() {
    $.ajax('/admin/wipe',
	   {type: "delete",
	    success: function (data) {
		alert("Wipe result:" + JSON.stringify(data));
		populateRecipes();
		populateIngredients();
		$('#recipe-panel').hide();
	    }
	   });
}

$(document).ready(function () {
    $.each("abcdefghijklmnopqrstuvwxyz".split(""), function(i, e) {
	$('#populate-letter').append($('<option/>', {value: e, text: e}));
    });

    populateIngredients();
    populateRecipes();
    $('#reset-filter').click(resetFilter);
    $('#populate-button').click(function() {
	$.post('/admin/populate/' + $('#populate-letter').val(), function(data) {
	    alert("Populated letter " + $('#populate-letter').val());
	    populateRecipes();
	    populateIngredients();
	});
    });
    $('#wipe-button').click(wipeStore);
});
