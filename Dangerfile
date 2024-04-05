# Only focus on changes for the current DIFF from the active PR
github.dismiss_out_of_range_messages

# Warn when there is a big PR.
warn("Pull Request size seems relatively large. If this PR contains multiple changes, please consider splitting it into multiple PRs. This way, reviewing the PR becomes easier and faster, and we can also find issues better because we need to focus on only change at a time.") if git.lines_of_code > 500

# To support multi module projects, find all lint reports
for lint_report_file in Dir.glob("**/lint-results-*.xml") do
    # Process Android-Lint results
    android_lint.skip_gradle_task = true # do this if lint was already run in a previous build step
    android_lint.report_file = lint_report_file
    android_lint.filtering_lines = true
    android_lint.lint(inline_mode: true)
end

for test_report_file in Dir.glob("**/build/test-results/**/TEST-*.xml") do
    junit.parse test_report_file
    junit.show_skipped_tests = true
    junit.skipped_headers = [:name, :classname]
    junit.headers = [:name, :classname]
    junit.report
end