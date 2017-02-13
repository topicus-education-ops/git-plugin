package hudson.plugins.git.extensions.impl.CleanAfterCheckout;

import lib.FormTagLib

def f = namespace(lib.FormTagLib);

f.entry(title:_("Exclude patterns")) {
	f.repeatableProperty(field:"excludePatterns")
}
f.entry(title:_("Timeout (in minutes) for clean operation"), field:"timeout") {
	f.number(clazz:"number", min:1, step:1)
}
