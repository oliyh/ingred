function populateIngredients() {
    $.get('/ingredients/', function (data) {
	$('#ingredients').empty();
	$.each(data, function(i, e) {
	    var ingredient = $('<li/>')
		.append($('<a/>', {class: 'label label-success', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { filterRecipesByIngredient(e.name); return false; }));
	    $('#ingredients').append(ingredient);
	});
    });
}

function filterRecipesByIngredient(ingredient) {
    $.get('/ingredients/' + ingredient, function (data) {
	$('#recipes').empty();
	$('#filterValue').text(ingredient);
	$('#filterType').text('ingredient');
	$('#filter').show();
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#recipes').append(recipe);
	});
    });
}

function filterRecipesByLetter(letter) {
    $.get('/recipes/' + letter + '/', function (data) {
	$('#recipes').empty();
	$('#filterValue').text(letter);
	$('#filterType').text('letter');
	$('#filter').show();
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#recipes').append(recipe);
	});
    });
}

function searchRecipes() {
    var searchTerm = $('#search-term').val();
    $.get('/recipes/search/' + searchTerm + '/', function (data) {
	$('#recipes').empty();
	$('#filterValue').text(searchTerm);
	$('#filterType').text('search term');
	$('#filter').show();
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#recipes').append(recipe);
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

function updateProgressBar(uri) {
    $.get(uri, function (data) {
	$('#progress').css('width', data.percent + '%');
	$('#elapsed').text(data['elapsed-human']);
	$('#total').text(data.total);
	$('#complete').text(data.complete);

	if (data.percent < 100) {
	    setTimeout(function() { updateProgressBar(uri); }, 1000);
	} else {
	    populateRecipes();
	    populateIngredients();
	}
    });
}

function populateRecipes() {
    $.get('/recipes/', function (data) {
	$('#filter').hide();
	$('#recipes').empty();
	$.each(data, function(i, e) {
	    var recipe = $('<li/>')
		.append($('<a/>', {class: 'label label-warning', href: e.url, title: e.name})
			.text(e.name)
			.click(function() { loadRecipe(e.url); return false; }));
	    $('#recipes').append(recipe);
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
    var alphabet = "abcdefghijklmnopqrstuvwxyz".split("");
    $.each(alphabet, function(i, e) {
	$('#populate-letter').append($('<option/>', {value: e, text: e}));
    });

    $.each(alphabet, function(i, e) {
	var letter = $('<li/>')
	    .append($('<a/>', {class: 'label label-info', href: 'recipes/' + e + '/', title: e})
		    .text(e)
		    .click(function() { filterRecipesByLetter(e); return false; }));
	$('#alphabet').append(letter);
    });

    $('#search-button').click(searchRecipes);

    populateIngredients();
    populateRecipes();
    $('#reset-filter').click(resetFilter);
    $('#populate-button').click(function() {
	$.post('/admin/populate/' + $('#populate-letter').val(), function(data) {
	    updateProgressBar(data.uri);
	});
    });
    $('#populate-all-button').click(function() {
	$.post('/admin/populate', function(data) {
	    updateProgressBar(data.uri);
	});
    });
    $('#wipe-button').click(wipeStore);
});
