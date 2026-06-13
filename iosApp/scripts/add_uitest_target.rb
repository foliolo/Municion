# One-off setup script: adds the `iosAppUITests` UI-test target (fastlane snapshot screenshots)
# to iosApp.xcodeproj and registers it in the shared iosApp scheme's Test action.
# Run from the repo root:  bundle exec ruby iosApp/scripts/add_uitest_target.rb
# Idempotent: exits without changes if the target already exists.
require 'xcodeproj'

project_path = File.expand_path('../iosApp.xcodeproj', __dir__)
project = Xcodeproj::Project.open(project_path)

if project.targets.any? { |t| t.name == 'iosAppUITests' }
  puts 'iosAppUITests target already exists — nothing to do.'
  exit 0
end

app_target = project.targets.find { |t| t.name == 'iosApp' } or abort('iosApp target not found')

target = project.new_target(:ui_test_bundle, 'iosAppUITests', :ios, '18.2')
target.add_dependency(app_target)

group = project.main_group.new_group('iosAppUITests', 'iosAppUITests')
file_refs = ['MunicionScreenshots.swift', 'SnapshotHelper.swift'].map { |f| group.new_file(f) }
target.add_file_references(file_refs)

target.build_configurations.each do |config|
  config.build_settings['TEST_TARGET_NAME'] = 'iosApp'
  config.build_settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'al.ahgitdevelopment.municion.uitests'
  config.build_settings['GENERATE_INFOPLIST_FILE'] = 'YES'
  config.build_settings['SWIFT_VERSION'] = '5.0'
  config.build_settings['CODE_SIGN_STYLE'] = 'Automatic'
  config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '18.2'
end

project.save
puts 'Added iosAppUITests target.'

scheme_path = Xcodeproj::XCScheme.shared_data_dir(project_path) + 'iosApp.xcscheme'
scheme = Xcodeproj::XCScheme.new(scheme_path)
testable = Xcodeproj::XCScheme::TestAction::TestableReference.new(target)
scheme.test_action.add_testable(testable)
scheme.save!
puts 'Registered iosAppUITests in the iosApp scheme Test action.'
