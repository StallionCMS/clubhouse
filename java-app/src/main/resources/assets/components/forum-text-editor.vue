
<style lang="scss">
 .forum-text-editor-vue {
     .btn-toolbar .btn-default  {
         line-height: 1em;
         padding: 5px 10px;
         width: 50px;
         .material-icons {
             width: 30px;
             font-size: 18px;
             line-height: 1em;
         }
     }
     .btn-toolbar .btn-default.btn-insert {
         width: 80px;
         height: 30px;
     }
     .btn-toolbar .btn .material-icons {
         font-size: 18px;
         line-height: 1em;
         vertical-align: -30%;
     }
     
     .md-editor > textarea {
         box-shadow: none;
         background: #fff;
         padding: 4px;
     }

 }
</style>

<template>
    <div class="forum-text-editor-vue">
        <textarea v-user-autocomplete tabindex="2" name="content" @input="onInput" @change="onChange" v-model="markdown" data-provide="markdown" rows="10" ></textarea>
        <text-editor-widget-modal  v-if="widgetModalShown" :widget-type="newWidgetType" :widget="editingWidget" @confirmed="onConfirmedWidget" @close="widgetModalShown=false" :config="config"></text-editor-widget-modal>
        <!-- username-auto-complete v-if="showUserAutoComplete" :term="userAutocompleteTerm"></username-auto-complete-->
    </div>
</template>

<script>
 module.exports = {
     props: {
         widgets: null,
         originalContent: null,
         html: null,
         config: null,
         allowHtml: {
             type: Boolean,
             default: false,
         },
         sanitizeHtml: {
             type: Boolean,
             default: true,
         },
         config: {
             default: function() {
                 return {};
             }
         },
         value: null
     },
     data: function() {
         var self = this;
         var markdown = null;
         var widgets = null;
         var hasModel = false;
         if (typeof(self.value) === typeof({})) {
             markdown = self.value.originalContent;
             if (!markdown && self.value.content) {
                 markdown = toMarkdown(self.value.content);
             }
             widgets = self.value.widgets;
             hasModel = true;
         } else if (typeof(self.originalContent) === 'string') {
             markdown = self.originalContent;
             widgets = self.widgets;
         } else if (typeof(self.content) === 'string') {
             markdown = self.fromHtml(self.content);
             widgets = self.widgets;
         }
         if (markdown === null || markdown === undefined) {
             markdown = '';
         }
         if (widgets === null || widgets === undefined) {
             widgets = [];
         }

         return {
             hasModel: hasModel,
             currentWidgets: JSON.parse(JSON.stringify(widgets)),
             markdown: markdown,
             widgetModalShown: false,
             editingWidget: null,
             newWidgetType: '',
             showUserAutoComplete: false,
             userAutocompleteTerm: ''
         };
     },
     created: function() {
         var self = this;
         this.dirty = false;
         this.recordDebouncedInput = stallion.debounce(self.onInputDebounced, 2000, false);
     },
     methods: {
         showEditWidget: function(widget) {
             var self = this;
             self.editingWidget = widget;
             self.widgetModalShown = true;
         },
         showWidgetModal: function(widgetType) {
             var self = this;
             self.newWidgetType = widgetType;
             self.widgetModalShown = true;
         },
         onConfirmedWidget: function(widget) {
             var self = this;
             var e = self.editor;
             console.log('markdown-textarea onConfirmedWidget ', widget);
             var self = this;
             var found = false;
             self.currentWidgets.forEach(function(existing) {
                 if (existing.id === widget.id) {
                     // Update the existing widget
                     found = true;
                     Object.keys(widget).forEach(function(key) {
                         existing[key] = widget[key];
                     });
                 }
             });
             if (!found) {
                 self.currentWidgets.push(widget);
             }

             $(self.$el).find('textarea').focus();
             var widgetToken = '[' + widget.type.toUpperCase() + ' ' + widget.guid + ']';
             if (!widget.inline) {
                 widgetToken = '  \n' + widgetToken + '  \n';
             }
             setTimeout(function() {
                 var selected = e.getSelection();
                 

                 // transform selection and set the cursor into chunked text
                 e.replaceSelection(widgetToken);
                 cursor = selected.end;
                 // Set the cursor
                 e.setSelection(cursor, cursor);
             }, 20);
         },
         toHtml: function(originalContent) {
             var self = this;
             var html = markdown.toHTML(originalContent, 'Maruku');
             //html = sanitizeHtml(html);
             self.currentWidgets.forEach(function(widget) {
                 var reText = "\\[(" + widget.type.toUpperCase() + ") (" + widget.guid + ")\\]";
                 var re = new RegExp(reText, 'g');
                 html = html.replace(re, function(full, type, guid) {
                     var $wrapper;
                     if (widget.inline) {
                         $wrapper = $('<span></span>');
                     } else {
                         $wrapper = $('<div></div>');
                     }
                     $wrapper.attr('id', 'st-widget-' + widget.guid);
                     $wrapper.attr('data-guid', widget.guid);
                     $wrapper.attr('data-type', widget.type);
                     $wrapper.addClass('st-widget');
                     $wrapper.addClass('st-widget-' + widget.type.toLowerCase().replace(/_/g, '-'));
                     $wrapper.html(widget.content);
                     return $wrapper[0].outerHTML;
                 });
             });
             return html;
         },
         fromHtml: function(html) {
             $html = $(html);
             $html.find('.st-widget').each(function() {
                 var ele = this;
                 var $ele = $(ele);
                 var guid = $ele.attr('data-guid');
                 var type = $ele.attr('data-type');
                 if (guid && type) {
                     ele.outerHTML = '[' + type.toUpperCase() + ' ' + guid + ']';
                 }
             });
             html = $html.html();
             return toMarkdown(html);
         },
         reset: function() {
             var self = this;
             self.currentWidgets = [];
             self.markdown = '';
             self.editor.setContent('');
         },
         setData: function(data) {
             var self = this;
             self.currentWidgets = JSON.parse(JSON.stringify(data.widgets || []));
             self.markdown = data.originalContentmarkdown;
             self.editor.setContent(data.originalContentmarkdown);
         },
         getData: function(withHtml) {
             var self = this;
             if (withHtml !== false) {
                 withHtml = true;
             }
             var allWidgets = JSON.parse(JSON.stringify(self.currentWidgets));
             var widgets = [];
             var originalContent = self.editor.getContent();
             allWidgets.forEach(function(widget) {
                 if (originalContent.indexOf(widget.guid + ']') > -1) {
                     widgets.push(widget);
                 }
             });
             
             var self = this;
             var data = {
                 originalContent: originalContent,
                 widgets: widgets
             }
             if (withHtml) {
                 data.content = self.toHtml(originalContent);
             }
             return data;
         },
         onKeyPress: function(evt) {
             var validCharacters = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_';
             var index = evt.target.selectionStart;
             debugger;
             console.log('index ', index, 'key', evt.key);
             if (evt.key === '@' && !this.showUserAutoComplete) {
                 if (index === 0) {
                     this.showUserAutoComplete = true;
                     this.userAutocompleteTerm = '';
                 } else if (validCharacters.indexOf(evt.target.value[index]) === -1) {
                     this.showUserAutoComplete = true;
                     this.userAutocompleteTerm = '';
                 }
             } else if (validCharacters.indexOf(evt.key) === -1) {
                 this.showUserAutoComplete = false;
                 this.userAutocompleteTerm = '';
             }

             if (this.showUserAutoComplete) {
                 if (evt.key === '@') {
                     return;
                 }
                 var atIndex = evt.target.value.lastIndexOf('@', index);
                 if (atIndex > -1 && atIndex < (evt.target.value.length -1)) {
                     var term = evt.target.value.substring(atIndex + 1, index) + evt.key;
                     console.log('new term ', term, atIndex + 1, index);
                     this.userAutocompleteTerm = term.toLowerCase();
                 } else if (atIndex > -1 && evt.target.key) {
                     this.userAutocompleteTerm = '@' + evt.target.key.toLowerCase();
                 }
                 
             }
             //showUserAutoComplete: false,
             //userAutocompleteTerm: ''

         },
         onInput: function(evt) {
             var self = this;
             if (self.hasModel) {
                 self.$emit('input', self.getData(), evt, self);
             } else {
                 self.$emit('input', self, evt);
             }
             console.log('onInput');
             
             this.dirty = true;
             this.recordDebouncedInput();
             //console.log('onInput', a, b);
         },
         onInputDebounced: function() {
             var self = this;
             if (!this.dirty) {
                 return;
             }
             this.dirty = false;
             console.log('inputDebounced! upwards!');
             self.$emit('input-debounced', this);
         },
         onChange: function(evt) {
             var self = this;
             if (self.hasModel) {
                 self.$emit('change', self.getData(), evt, self);
             } else {
                 self.$emit('change', self, evt);
             }
             this.dirty = true;
             self.onInputDebounced();
             //self.$emit('change', self, evt);
         }
     },
     mounted: function() {
         var self = this;
         var config = $.extend({}, self.config);
         if (!config.iconlibrary) {
             config.iconlibrary = 'material';
         }
         if (!config.onPreview) {
             config.onPreview = function(editor) {
                 return self.toHtml(editor.getContent());
             };
         }
         if (config.resize === undefined) {
             config.resize = "vertical";
         }
         if (config.autofocus === true) {
             setTimeout(function(){$(self.$el).find('textarea').focus()}, 100);
         }
         config.hiddenButtons = 'cmdImage cmdPreview';
         if (!config.additionalButtons) {
             config.additionalButtons = [
                 [{
                     name: "groupLink",
                     data: [{
                         name: "cmdBeer",
                         toggle: false, // this param only take effect if you load bootstrap.js
                         title: "Beer",
                         icon: '',
                         btnText: 'Insert +',
                         btnClass: 'btn btn-insert',
                         //icon: {material: "material-icons material-local-drink"},
                         callback: function(e){
                             self.showWidgetModal();
                         }
                     }]
                 }]
             ];
         }
         self.editor = $(this.$el).find('textarea').markdown(config).data('markdown');
         //$(this.$el).find('textarea').autogrow();
     }
 };
</script>
