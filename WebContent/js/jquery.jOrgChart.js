/**
 * jQuery org-chart/tree plugin.
 *
 * Author: Wes Nolte
 * http://twitter.com/wesnolte
 *
 * Based on the work of Mark Lee
 * http://www.capricasoftware.co.uk
 *
 * This software is licensed under the Creative Commons Attribution-ShareAlike
 * 3.0 License.
 *
 * See here for license terms:
 * http://creativecommons.org/licenses/by-sa/3.0
 */
;(function ( $, window, document, undefined ) {

	// Create the defaults once
    var pluginName = 'jOrgChart',
        defaults = {
			chartElement : 'body',
			depth		 : -1,
			chartClass	 : "jOrgChart",
			dragAndDrop	 : false,
			defaultDepth : -1,
			source		 : null
        };

    // The actual plugin constructor
    function Plugin( element, options ) {
        this.element = element;

        this.options = $.extend( {}, defaults, options) ;

        this._defaults = defaults;
        this._name = pluginName;

        this.init();
    }

	Plugin.prototype.init = function () {
        // Place initialization logic here
        // You already have access to the DOM element and the options via the instance, 
        // e.g., this.element and this.options
		if (this.options.source === null || this.options.source.length === 0) {
			// Expand and contract nodes
			this.build();
			
		} else {
			// Restore and setting
			var data = this.options.source.split("\t")
			if (data.length===2) {
				$(this.element).html(data[1]);
				$(this.options.chartElement).html(data[0]);
			}
			if (this.options.dragAndDrop) {
				// drag&drop enable (= lock off)
				this.build();
			} else {
				// drag&drop disable (= lock on)
				this.dragAndDrop();
			}
		}


		// Expand and contract nodes
		var that = this;
		$(this.options.chartElement).off("click", "div.node")
			.on("click", "div.node", function(){
			var $this = $(this);
			var $tr = $this.closest("tr");
			$tr.nextAll("tr").fadeToggle("fast");
	
			if($tr.hasClass('contracted')){
				$this.css('cursor','n-resize');
				//$tr.addClass('expanded');
			}else{
				$this.css('cursor','s-resize');
				//$tr.addClass('contracted');
			}
		});

    };

	// Expand and contract nodes
	Plugin.prototype.build = function () {
		$(this.options.chartElement).children().remove();
		var $appendTo = $(this.options.chartElement);

		// build the tree
		$this = $(this.element);
		var $container = $("<div class='" + this.options.chartClass + "'/>");
		if($this.is("ul")) {
			buildNode($this.find("li:first"), $container, 0, this.options);
		}
		else if($this.is("li")) {
			buildNode($this, $container, 0, this.options);
		}
		$appendTo.append($container);

		// add drag and drop if enabled
		this.dragAndDrop();
		
		// collapse level
		collapseNode($appendTo.find("tr:first"), 0, this.options);

    };

	// add drag and drop if enabled
	Plugin.prototype.dragAndDrop = function () {
		var that = this;
		if(!that.options.dragAndDrop) {
			return;
		}

		$('div.node').draggable({
			cursor		: 'move',
			distance	: 40,
			helper		: 'clone',
			opacity		: 0.8,
			revert		: true,
			revertDuration : 100,
			snap		: 'div.node.expanded',
			snapMode	: 'inner',
			stack		: 'div.node'
		});

		$('div.node').droppable({
			accept		: '.node',
			activeClass : 'drag-active',
			hoverClass	: 'drop-hover'
		});

		// Drag start event handler for nodes
		$('div.node').bind("dragstart", function handleDragStart( event, ui ){

		var sourceNode = $(this);
		sourceNode.parentsUntil('.node-container')
					 .find('*')
					 .filter('.node')
					 .droppable('disable');
		});

		// Drag stop event handler for nodes
		$('div.node').bind("dragstop", function handleDragStop( event, ui ){

			/* reload the plugin */
			that.build();
		});

		// Drop event handler for nodes
		$('div.node').bind("drop", function handleDropEvent( event, ui ) {
			var sourceNode = ui.draggable;
			var targetNode = $(this);

		// finding nodes based on plaintext and html
		// content is hard!
		var targetLi = $('li').filter(function(){

			li = $(this).clone()
						.children("ul,li")
						.remove()
					.end();

			return li.html() == targetNode.html();
		});

		var sourceLi = $('li').filter(function(){

			li = $(this).clone()
						.children("ul,li")
						.remove()
					.end();

			return li.html() == sourceNode.html();
		});

		var sourceliClone = sourceLi.clone();
		var sourceUl = sourceLi.parent('ul');

		if(sourceUl.children('li').size() > 1){
			sourceLi.remove();
		}else{
			sourceUl.remove();
		}

		if(targetLi.children('ul').size() >0){
			targetLi.children('ul').append('<li>'+sourceliClone.html()+'</li>');
		}else{
			targetLi.append('<ul><li>'+sourceliClone.html()+'</li></ul>');
		}

		}); // handleDropEvent
		
	};

	// rebuild contract nodes
	Plugin.prototype.rebuild = function(options){
		this.options = $.extend( {}, this.options, options ) ;
		this.init();
	};

	// Method that recursively builds the tree
	var buildNode = function ($node, $appendTo, level, opts) {

		var $table = $("<table cellpadding='0' cellspacing='0' border='0'/>");
		var $tbody = $("<tbody/>");

		// Construct the node container(s)
		var $nodeRow = $("<tr/>").addClass("node-cells");
		var $nodeCell = $("<td/>").addClass("node-cell").attr("colspan", 2);
		var $childNodes = $node.children("ul:first").children("li");

		if($childNodes.length > 1) {
			$nodeCell.attr("colspan", $childNodes.length * 2);
		}
		// Draw the node
		// Get the contents - any markup except li and ul allowed
		var $nodeContent = $node.clone()
					.children("ul,li")
					.remove()
					.end()
					.html();

		$nodeDiv = $("<div>").addClass("node").append($nodeContent);

		$nodeCell.append($nodeDiv);
		$nodeRow.append($nodeCell);
		$tbody.append($nodeRow);

		if($childNodes.length > 0) {
		// if it can be expanded then change the cursor
		$nodeDiv.css('cursor','n-resize').addClass('expanded');

		// recurse until leaves found (-1) or to the level specified
		if(opts.depth == -1 || (level+1 < opts.depth)) {
			var $downLineRow = $("<tr/>");
			var $downLineCell = $("<td/>").attr("colspan", $childNodes.length*2);
			$downLineRow.append($downLineCell);

			// draw the connecting line from the parent node to the horizontal line
			$downLine = $("<div></div>").addClass("line down");
			$downLineCell.append($downLine);
			$tbody.append($downLineRow);

			// Draw the horizontal lines
			var $linesRow = $("<tr/>");
			$childNodes.each(function() {
				var $left = $("<td/>").addClass("line left top");
				$left.append($("<div></div>").addClass("line"));
				var $right = $("<td/>").addClass("line right top");
				$right.append($("<div></div>").addClass("line"));
				$linesRow.append($left).append($right);
			});

			// horizontal line shouldn't extend beyond the first and last child branches
			$linesRow.find("td:first")
				 .removeClass("top")
				 .end()
				 .find("td:last")
				 .removeClass("top");

			$tbody.append($linesRow);
			var $childNodesRow = $("<tr/>");
			$childNodes.each(function() {
				var $td = $("<td class='node-container'/>");
				$td.attr("colspan", 2);
		 		// recurse through children lists and items
				buildNode($(this), $td, level+1, opts);
				$childNodesRow.append($td);
			});
			}
			$tbody.append($childNodesRow);
		}

		$table.append($tbody);
		$appendTo.append($table);
	};

	// Method that collapse builds the tree level
	var collapseNode = function($node, level, opts){
		if (opts.defaultDepth === -1) return;
		// The child element (td) of the last line is acquired within the same table.
		$childNodes = $node.nextAll("tr:last").children("td");

		if (level+1 === opts.defaultDepth){
			// toggle
			$node.nextAll("tr").toggle();
		} else if ($childNodes.length > 0) {
			// It is recovery processing about the element (tr) of the beginning in a child element.
			$childNodes.each(function() {
				collapseNode($(this).find("tr:first"), level+1, opts);
			});
		}
	}

    // A really lightweight plugin wrapper around the constructor, 
    // preventing against multiple instantiations
    $.fn[pluginName] = function ( options ) {
        return this.each(function () {
            if (!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new Plugin( this, options ));
			}
        });
    };
	// return instantiation
	$.fn['get'+pluginName] = function(){
		return $.data($(this).get(0), 'plugin_' + pluginName);
	}

})( jQuery, window, document );