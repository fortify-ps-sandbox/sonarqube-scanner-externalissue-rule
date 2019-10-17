# SonarQube external issue data rules plugin

Disclaimer
====
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
DEALINGS IN THE SOFTWARE.

Fortify
====
Although this project is hosted on the Fortify Professional Services GitHub page,
this SonarQube plugin is not related to any of the Fortify products. However, it
could potentially be utilized for future SonarQube/Fortify integrations, by generating
Fortify issue data and category descriptions in SonarQube generic issue data format.

Potentially this could provide a more loosely coupled integration between Fortify and SonarQube, replacing most of the functionality provided by the current Fortify SonarQube integration (see https://github.com/fortify-ps/fortify-integration-sonarqube).
This native Fortify SonarQube integration relies on various deprecated SonarQube API's and features, so could potentially cease functioning with future SonarQube versions.

Introduction
====
The SonarQube Generic Issue Data functionality (see https://docs.sonarqube.org/latest/analysis/generic-issue/) allows for importing
external issues, but lacks functionality for importing rule data.

With the example listed on the SonarQube page, you will for example see
an external issue 'fully-fleshed issue' in your SonarQube project, but
clicking the 'See Rule' link will just show a generic rule message without
any rule-specific details.

This plugin allows for adding a `rules` array to the external issue data
JSON file(s), with each `Rule` entry in the array containing the following
properties:

- engineId
- ruleId
- name
- severity
- description
- type

Based on this information, this plugin will generate a SonarQube ad-hoc rule for every
rule defined in the `rules` array. Assuming the ruleId's referenced by the individual
issues match a corresponding rule in the `rules` array, SonarQube will now display
the appropriate rule description and other information when clicking the 'See Rule' link.

Following is an example of the expected input format:

```json
{ "issues": [
    {
      "engineId": "test",
      "ruleId": "rule1",
      "severity":"BLOCKER",
      "type":"CODE_SMELL",
      "primaryLocation": {
        "message": "fully-fleshed issue",
        "filePath": "src/main/java/com/fortify/sca/plugins/maven/samples/EightBall.java",
        "textRange": {
          "startLine": 5,
          "endLine": 5,
          "startColumn": 9,
          "endColumn": 14
        }
      },
      "effortMinutes": 90,
      "secondaryLocations": [
        {
          "message": "cross-file 2ndary location",
          "filePath": "src/main/java/com/fortify/sca/plugins/maven/samples/EightBall.java",
          "textRange": {
            "startLine": 10,
            "endLine": 10,
            "startColumn": 6,
            "endColumn": 38
          }
        }
      ]
    },
    {
      "engineId": "test",
      "ruleId": "rule2",
      "severity": "INFO",
      "type": "BUG",
      "primaryLocation": {
        "message": "minimal issue raised at file level",
        "filePath": "src/main/java/com/fortify/sca/plugins/maven/samples/EightBall.java"
      }
    }
],
  "rules": [
    {
	  "engineId": "test",
	  "ruleId": "rule1",
	  "name": "rule1",
	  "severity": "INFO",
	  "description": "This is a description for rule1",
	  "type": "BUG"
	},
	{
	  "engineId": "test",
	  "ruleId": "rule2",
	  "name": "rule2",
	  "severity": "BLOCKER",
	  "description": "This is a description for rule2",
	  "type": "VULNERABILITY"
	}
]}
```

Notes
====
The plugin currently doesn't check whether an ad-hoc rule with a given `ruleId` has already been generated during the current scan. It seems like SonarQube 7.9.1 silently accepts duplicate
rule id's (using the latest defined rule details for any given rule id), but other/future SonarQube versions could potentially raise an error if duplicate rule id's are defined.

As such, tools generating the external issue data should prevent outputting duplicate rule id's. If you provide multiple external issue data files during a SonarQube scan, there is still a risk that different files define the same rule id's. 


Building from source
====

Prerequisites
----

### Tools
In order to retrieve the source code and build the project, you will need to have the following tools installed:

* Git client
* Maven 3.x

Building the project
----
Once all prerequisites have been met, you can use the following commands to build this project:

* `git clone https://github.com/fortify-ps/sonarqube-scanner-externalissue-rule.git`
* `cd sonarqube-scanner-externalissue-rule`
* `git checkout [branch or tag that you want to build]`
* `mvn clean package`

Once completed, the SonarQube plugin jar can be found in the target directory. The
plugin can be installed by copying the plugin JAR to your SonarQube extensions/plugins
directory, and restarting SonarQube.
