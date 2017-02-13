package hudson.plugins.git.extensions.impl.CleanAfterCheckout.ExcludePattern;

def f = namespace(lib.FormTagLib);

f.repeatableDeleteButton()
f.entry(title:_("Exclude pattern"), field:"pattern") {
	f.textbox()
}
