# Contributing Guidelines

**Do** follow [the seven rules of a great Git commit message][1].

**Do** follow [the Clojure Style Guide][2].

**Do** include tests for your change when appropriate.

**Do** ensure that the CI checks pass.

**Do** squash the commits in your PR to remove corrections
irrelevant to the code history, once the PR has been reviewed.

**Do** feel free to pester the project maintainers about the PR if it
hasn't been responded to. Sometimes notifications can be missed.

**Don't** include more than one feature or fix in a single PR.

**Don't** include changes unrelated to the purpose of the PR. This
includes changing the project version number, adding lines to the
`.gitignore` file, or changing the indentation or formatting.

**Don't** open a new PR if changes are requested. Just push to the
same branch and the PR will be updated.

**Don't** overuse vertical whitespace; avoid multiple sequential blank
lines.

**Don't** docstring private vars or functions.

[1]: https://chris.beams.io/posts/git-commit/#seven-rules
[2]: https://github.com/bbatsov/clojure-style-guide
