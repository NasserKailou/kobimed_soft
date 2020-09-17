$(function() {
    $.extend(true, $.fn.dataTable.defaults, {
    		"language": {
    			"lengthMenu": "Afficher _MENU_ entrées",
    			"zeroRecords": "Aucun résultat ne correspond à votre recherche",
    			"info": "_TOTAL_ résultats, page _PAGE_/_PAGES_ de _START_ à _END_",
    			"infoEmpty": "Aucun résultat",
    			"infoFiltered":	"(sur _MAX_ entrées au total)",
    			"search": "Rechercher",
    			"searchPlaceholder": "Rechercher"
    		}
    	});
    	$('.dta-tbl').DataTable();
});