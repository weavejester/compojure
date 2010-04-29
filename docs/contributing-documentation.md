---
layout: doc
title: Contributing Documentation
---

Compojure's documentation is hosted by [GitHub Pages][1], and stored in the
[`gh-pages` branch][2] of the Compojure repository.

To begin contributing, fork [Compojure on GitHub][3], and then clone a local
copy of your fork using [Git][4]:

{% highlight sh %}
git clone git@github.com:your-account/compojure.git
cd compojure
{% endhighlight %}

Once you have the repository, you'll need to create a [tracking branch][5] for
the remote `gh-pages` branch:

{% highlight sh %}
git branch --track gh-pages origin/gh-pages
git checkout gh-pages
{% endhighlight %}

Now you're ready to begin writing documentation! The documentation is stored
in the `docs` directory, and by convention written in [Markdown][6] format,
with [Jekyll][7] providing extensions such as syntax highlighting.

The [`docs/getting-started.md`][8] file is a good example of the documentation
format Compojure uses. Try to make your documentation file readable, and each
line within 80 characters in width. If you are adding a new file, remember to
add the link to the `index.html` file. 

To see how your Markdown files will look as HTML, you'll need to download and
install Jekyll:

{% highlight sh %}
sudo gem install jekyll
{% endhighlight %}

You can then run Jekyll in server mode in the root directory of your Compojure
repository:

{% highlight sh %}
jekyll --pygments --auto --server
{% endhighlight %}

This will start a web server that you can access at <http://localhost:4000>.
It will automatically update the HTML whenever you make changes to your file.
Just hit the refresh button on your browser.

Once you are happy with your changes, commit and push:

{% highlight sh %}
git commit -am "Added documentation on sessions"
git push
{% endhighlight %}

And then ask [me][9] to merge your changes, either by sending me a pull
request via GitHub, or just emailing me.

[1]:http://pages.github.com
[2]:http://github.com/weavejester/compojure/tree/gh-pages
[3]:http://github.com/weavejester/compojure
[4]:http://git-scm.com
[5]:http://book.git-scm.com/4_tracking_branches.html
[6]:http://daringfireball.net/projects/markdown
[7]:http://github.com/mojombo/jekyll
[8]:http://github.com/weavejester/compojure/raw/gh-pages/docs/getting-started.md
[9]:http://github.com/weavejester
