# Maven Deployment Guide - AstroidMC Repository

## 📦 Repository Information

**Web UI:** https://maven.astroidmc.com/  
**Releases:** https://maven.astroidmc.com/#browse/browse:maven-releases  
**Snapshots:** https://maven.astroidmc.com/#browse/browse:maven-snapshots

---

## 🔧 Setup

### 1. Configure Maven Credentials

Edit your Maven settings file: `~/.m2/settings.xml` (Windows: `C:\Users\{username}\.m2\settings.xml`)

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                              https://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>astroidmc-releases</id>
            <username>YOUR_NEXUS_USERNAME</username>
            <password>YOUR_NEXUS_PASSWORD</password>
        </server>
        <server>
            <id>astroidmc-snapshots</id>
            <username>YOUR_NEXUS_USERNAME</username>
            <password>YOUR_NEXUS_PASSWORD</password>
        </server>
    </servers>
</settings>
```

**Important:** The `<id>` must match the repository ID in `pom.xml`.

---

## 🚀 Deploying to Maven

### Deploy Release Version

1. Make sure version in `pom.xml` does NOT contain `-SNAPSHOT`:
```xml
<version>1.0</version>
```

2. Deploy only the API module:
```bash
cd proxy-api
mvn clean deploy
```

Or from root:
```bash
mvn clean deploy -pl proxy-api
```

### Deploy Snapshot Version

1. Update version in root `pom.xml` to include `-SNAPSHOT`:
```xml
<version>1.1-SNAPSHOT</version>
```

2. Deploy:
```bash
cd proxy-api
mvn clean deploy
```

---

## 📥 Using the API in Your Project

Add to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>astroidmc-releases</id>
        <url>https://maven.astroidmc.com/repository/maven-releases/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.tomkeuper.bedwars</groupId>
        <artifactId>proxy-api</artifactId>
        <version>1.0</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

For snapshots, add the snapshot repository:
```xml
<repository>
    <id>astroidmc-snapshots</id>
    <url>https://maven.astroidmc.com/repository/maven-snapshots/</url>
</repository>
```

---

## 🔍 Verify Deployment

After deployment, verify at:
- **Releases:** https://maven.astroidmc.com/#browse/browse:maven-releases:com%2Ftomkeuper%2Fbedwars
- **Snapshots:** https://maven.astroidmc.com/#browse/browse:maven-snapshots:com%2Ftomkeuper%2Fbedwars

---

## 📝 Notes

- **Only deploy proxy-api:** The proxy-plugin is NOT deployed to Maven (distributed as JAR)
- **Version consistency:** Child modules inherit version from parent POM
- **Clean before deploy:** Always run `mvn clean` before deploying
- **Test locally first:** Run `mvn clean install` to test before deploying

---

## ❗ Troubleshooting

### Authentication Failed
- Check credentials in `~/.m2/settings.xml`
- Verify server ID matches repository ID in pom.xml
- Ensure you have permission to deploy to the repository

### 400 Bad Request
- Version might already exist in releases repository
- Releases are immutable - increment version or use snapshots

### Cannot Find Artifact
- Repository URL might be incorrect
- Check if deployment actually succeeded
- Verify groupId/artifactId/version are correct

### Deploy Skipped
- Check that `<skip>false</skip>` is set in proxy-api pom.xml
- Parent POM has `<skip>true</skip>` which is correct (prevents deploying the aggregator)

---

## 🔄 Full Build Process

```bash
# 1. Clean everything
mvn clean

# 2. Build entire project (plugin + api)
mvn package

# 3. Install to local repository (optional, for testing)
mvn install -pl proxy-api

# 4. Deploy API to remote repository
mvn deploy -pl proxy-api

# 5. Find built plugin JAR at:
# proxy-plugin/target/proxy-plugin-1.0.jar
```

---

## 📊 Deployment Checklist

- [ ] Update version in root `pom.xml`
- [ ] Configure Maven credentials in `~/.m2/settings.xml`
- [ ] Run `mvn clean` first
- [ ] Test build locally with `mvn package`
- [ ] Deploy with `mvn deploy -pl proxy-api`
- [ ] Verify on web UI: https://maven.astroidmc.com/
- [ ] Update version in dependent projects
- [ ] Tag release in Git (for releases only)

---

## 🏷️ Version Naming Convention

**Releases:**
- `1.0` - Major release
- `1.1` - Minor release
- `1.0.1` - Patch release

**Snapshots:**
- `1.1-SNAPSHOT` - Development version
- `2.0-SNAPSHOT` - Next major version in development

Snapshots can be overwritten, releases are immutable.

