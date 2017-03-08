Feature: Run ACME experiment

  Background:
    When I want to create an experiment named Hello world
    And the baseline is sale of apples
    And the success is send email to Moos
    And the failure is send email to Sam
    And the time is 2 seconds

  Scenario: Run experiment with successful outcome
    And the assume is sale of apples has risen by 8 per cent
    And I create the template
    Then the template is correct
    When I start the experiment
    Then the experiment is ready
    And the experiment is started
    When I wait 3 seconds
    Then the experiment is completed successfully
    And the environment key result has value sending success mail to Moos

  Scenario: Cannot start experiment with incorrect template
    And the assume is sale of apples has done nothing
    And I create the template
    Then the template is not correct
    And I cannot start the experiment

  Scenario: Create template with invalid assume glueline, then correct it
    And the assume is sale of apples has done nothing
    And I create the template
    Then the template is not correct
    When I update the assume line of the current template to sale of apples has risen by 8 per cent
    Then the template is correct

  Scenario: Create template with valid assume glueline, then change it to an incorrect value
    And the assume is sale of apples has risen by 8 per cent
    And I create the template
    Then the template is correct
    When I update the assume line of the current template to sale of apples has done nothing
    Then the template is not correct

  Scenario: Run experiment with failed outcome
    And the assume is sale of apples has risen by 11 per cent
    And I create the template
    And I start the experiment
    Then the experiment is ready
    And the experiment is started
    When I wait 3 seconds
    Then the experiment is completed unsuccessfully
    And the environment key result has value sending failure mail to Sam