# Quick Maven Deploy Commands

## 🚀 Deploy to AstroidMC Maven

### Deploy API (Release)
```bash
mvn clean deploy -pl proxy-api
```

### Deploy API (Snapshot)
First update version in pom.xml to include `-SNAPSHOT`, then:
```bash
mvn clean deploy -pl proxy-api
```

### Build Plugin JAR
```bash
mvn clean package
```
Output: `proxy-plugin/target/proxy-plugin-1.0.jar`

### Full Build + Deploy
```bash
mvn clean package && mvn deploy -pl proxy-api
```

---

## 📋 Pre-Deploy Checklist
1. ✅ Credentials in `~/.m2/settings.xml`
2. ✅ Version updated in root `pom.xml`
3. ✅ Code committed to git
4. ✅ Tests passing

---

## 🔗 Quick Links
- Maven UI: https://maven.astroidmc.com/
- Releases: https://maven.astroidmc.com/#browse/browse:maven-releases
- Snapshots: https://maven.astroidmc.com/#browse/browse:maven-snapshots

