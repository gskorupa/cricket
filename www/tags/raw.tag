<raw>
  this.updateContent = function () {
        this.root.innerHTML = opts.html;
  }
  this.on('update', function() {
        this.updateContent();
  });
  this.updateContent();
</raw>
