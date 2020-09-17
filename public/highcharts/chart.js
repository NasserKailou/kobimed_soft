var processed_json = new Array(); 
$.getJSON('http://localhost:9000/json-cons', function(data){
	
	 for (i = 0; i < data.length; i++){
         processed_json.push([data[i].key, data[i].value]);
     }
	 // alert("Data: " + processed_json);
	  return processed_json;
	});
 

$(document).ready(function () {
	//recuperation des donner par json
	 var data2 = $("#listesID").val();
	 //alert(data2);
	 var donnees = data2.split("#");
	 //alert(donnees);
	 
	 var typeConsul = [];
	 var assureur = [];
	 var nombre = [];
	 
    for (var i = 0; i < donnees.length; i++) {
    	assureur.push(donnees[i].split("@")[0]);
    	nombre.push(donnees[i].split("@")[1]);
    	typeConsul.push(donnees[i].split("@")[0], donnees[i].split("@")[1]);
        
    }
    	
//alert(typeConsul);

		Highcharts.chart('container1', {
		  chart: {
		    plotBackgroundColor: null,
		    plotBorderWidth: null,
		    plotShadow: false,
		    type: 'pie'
		  },
		  title: {
		    text: 'Browser market shares in January, 2018'
		  },
		  tooltip: {
		    pointFormat: '{series.name}: <b>{point.percentage:.1f}%</b>'
		  },
		  plotOptions: {
		    pie: {
		      allowPointSelect: true,
		      cursor: 'pointer',
		      dataLabels: {
		        enabled: true,
		        format: '<b>{point.name}</b>: {point.percentage:.1f} %',
		        connectorColor: 'silver'
		      }
		    }
		  },
		  series: [{
		    name: 'Share',
		    data: [
		      { name: 'Chrome', y: 38.41 },
		      { name: 'Internet Explorer', y: 11.84 },
		      { name: 'Firefox', y: 10.85 },
		      { name: 'Edge', y: 16.67 },
		      { name: 'Safari', y: 4.18 },
		      { name: 'Other', y: 18.05 }
		    ]
		  }]
		});
		
		
		Highcharts.chart('container2', {
		  chart: {
		    type: 'column',
		    options3d: {
		      enabled: true,
		      alpha: 10,
		      beta: 25,
		      depth: 70
		    }
		  },
		  title: {
		    text: '3D chart with null values'
		  },
		  subtitle: {
		    text: 'Notice the difference between a 0 value and a null point'
		  },
		  plotOptions: {
		    column: {
		      depth: 25
		    }
		  },
		  xAxis: {
		    categories: assureur,
		    labels: {
		      skew3d: true,
		      style: {
		        fontSize: '16px'
		      }
		    }
		  },
		  yAxis: {
			  categories: nombre,
			    
		        title: {
		            text: 'Value',
		            margin: 80
		        }
		    },
		  series: [{
			type: 'column',
		    name: assureur,
		    data: nombre
		  }]
		});
		
		Highcharts.chart('container3', {
		    title: {
		        text: 'Combination chart'
		    },
		    xAxis: {
		        categories: ['Apples', 'Oranges', 'Pears', 'Bananas', 'Plums']
		    },
		    labels: {
		        items: [{
		            html: 'Total fruit consumption',
		            style: {
		                left: '50px',
		                top: '18px',
		                color: ( // theme
		                    Highcharts.defaultOptions.title.style &&
		                    Highcharts.defaultOptions.title.style.color
		                ) || 'black'
		            }
		        }]
		    },
		    series: [{
		        type: 'column',
		        name: 'Jane',
		        data: [3, 2, 1, 3, 4]
		    }, {
		        type: 'column',
		        name: 'John',
		        data: [2, 3, 5, 7, 6]
		    }, {
		        type: 'column',
		        name: 'Joe',
		        data: [4, 3, 3, 9, 0]
		    }, {
		        type: 'spline',
		        name: 'Average',
		        data: [3, 2.67, 3, 6.33, 3.33],
		        marker: {
		            lineWidth: 2,
		            lineColor: Highcharts.getOptions().colors[3],
		            fillColor: 'white'
		        }
		    }, {
		        type: 'pie',
		        name: 'Total consumption',
		        data: [{
		            name: 'Jane',
		            y: 13,
		            color: Highcharts.getOptions().colors[0] // Jane's color
		        }, {
		            name: 'John',
		            y: 23,
		            color: Highcharts.getOptions().colors[1] // John's color
		        }, {
		            name: 'Joe',
		            y: 19,
		            color: Highcharts.getOptions().colors[2] // Joe's color
		        }],
		        center: [100, 80],
		        size: 100,
		        showInLegend: false,
		        dataLabels: {
		            enabled: false
		        }
		    }]
		});
		
		 Highcharts.chart('container4', {
		    chart: {
		        type: 'pie',
		        options3d: {
		            enabled: true,
		            alpha: 45
		        }
		    },
		    title: {
		        text: 'Poucentage d\'exécution des consultations par type de spécialité'
		    },
		    subtitle: {
		        text: 'graphe de synthèse'
		    },
		    plotOptions: {
		        pie: {
		            innerSize: 100,
		            depth: 45
		        }
		    },
		    series:[{
		        name: 'Delivered amount',
		        data: [
		            ['Bananas', 8],
		            ['Kiwi', 3],
		            ['Mixed nuts', 1],
		            ['Oranges', 6],
		            ['Apples', 8],
		            ['Pears', 4],
		            ['Clementines', 4],
		            ['Reddish (bag)', 1],
		            ['Grapes (bunch)', 1]
		        ]
		    }]
		 });
		 
});
