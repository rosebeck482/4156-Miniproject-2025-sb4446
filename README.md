### Static bug finding with PMD

Installation:

```bash
brew install pmd
```

Run:
```bash
cd 4156-Miniproject-2025-Students/IndividualProject
pmd check -d ./src/main/java \
          -R rulesets/java/quickstart.xml \
          -f text
```
