# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.

from pathlib import Path

import mozunit

from moztreedocs import _is_excluded_path


def test_exclude_patterns_match_windows_paths(tmp_path):
    topsrcdir = Path(tmp_path)
    debugger_doc = topsrcdir / "js" / "src" / "doc" / "Debugger" / "index.md"

    assert _is_excluded_path(debugger_doc, topsrcdir, ["js/src/doc/Debugger"])
    assert not _is_excluded_path(
        topsrcdir / "js" / "src" / "doc" / "DebuggerTools" / "index.md",
        topsrcdir,
        ["js/src/doc/Debugger"],
    )


if __name__ == "__main__":
    mozunit.main()
