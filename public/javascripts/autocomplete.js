/**
* @author anasser.
* @since 03-02-2020
* recherche avec auto completion pour les patients
*/
$(function () {
            /*--recherche dynamic utilisateur, typeahead--*/
            $("#telPatient").autocomplete({
                serviceUrl: '/json-patient',
                dataType: 'json',

                //params: {paramnam: paramvalue},
                minChars: 3,
                width: 300,
                transformResult: function (response) {
                    return {
                        suggestions: $.map(response.options, function (item) {
                            return {value: "" + item.telephone, data: item};
                        })
                    };
                },
                formatResult: function (suggestion, currentValue) {
                    var result = suggestion;
                    if (suggestion && suggestion.data && suggestion.data.nomPrenom) {
                        result = suggestion.data.telephone + '-' + suggestion.data.nomPrenom;
                    }
                    return result;
                },
                onSelect: function (suggestion) {
                        $("#nomPrenomPatient").val(suggestion.data.nomPrenom);
                        $("#age").val(suggestion.data.age);
                    }

            });

        });



/**
* @author anasser.
* @since 03-02-2020
* recherche avec auto completion pour les type de partenaire
*/
$(function () {
            /*--recherche dynamic utilisateur, typeahead--*/
            $("#partenaire").autocomplete({
                serviceUrl: '/json-partenaire',
                dataType: 'json',

                //params: {paramnam: paramvalue},
                minChars: 3,
                width: 300,
                transformResult: function (response) {
                    return {
                        suggestions: $.map(response.option, function (item) {
                            return {value: "" + item.libelle, data: item};
                        })
                    };
                },
                formatResult: function (suggestion, currentValue) {
                    var result = suggestion;
                    if (suggestion && suggestion.data && suggestion.data.libelle) {
                        result = suggestion.data.libelle ;
                    }
                    return result;
                },
                onSelect: function (suggestion) {
                        $("#partenaireid").val(suggestion.data.id);
                        $("#taux").val(suggestion.data.tauxCouverture);
                    }

            });

        });

$(document).ready(function(){
	//$( ".pl" ).hide();
	    $(document).on('change', '.isP', function () {
	    var ra = document.getElementsByName('rad');
	      if(ra[0].checked)
	      $( ".pl" ).hide();
	      else
	      $( ".pl" ).show();
	    });


	    $(document).on('change', '.isT', function () {
	        var tr = document.getElementsByName('trans');
	          if(tr[0].checked)
	          $( ".transfert" ).hide();
	          else
	          $( ".transfert" ).show();
	        });
	});