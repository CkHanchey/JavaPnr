# PNR Gov - Startup Guide

This guide provides step-by-step instructions to build and run the PNR Gov application.

## 📋 Prerequisites Check

### 1. Verify Java Installation

```powershell
java -version
```

Expected output: `openjdk version "17.0.17"` or higher

If not installed, download from: https://adoptium.net/

### 2. Verify Node.js and npm

```powershell
node --version  # Should be 18.x or higher
npm --version   # Should be 9.x or higher
```

If not installed, download from: https://nodejs.org/

### 3. Verify Gradle (Optional)

The project includes Gradle wrapper, so local installation is not required.

```powershell
.\gradlew --version
```

## 🚀 Backend Setup

### Step 1: Navigate to Project Root

```powershell
cd F:\DEV\Java\PNRGOV
```

### Step 2: Clean and Build

```powershell
.\gradlew clean build
```

This will:
- Download all dependencies
- Compile Java source code
- Run unit tests
- Create JAR files in `build/libs/`

**Expected output:**
```
BUILD SUCCESSFUL in 30s
```

### Step 3: Run Backend

```powershell
.\gradlew :pnrgov-api:bootRun
```

**Expected output:**
```
Started PnrGovApplication in X.XXX seconds
```

### Step 4: Verify Backend

Open a browser and navigate to:

- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health Check: http://localhost:8080/actuator/health

Or test with PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/edifact/generate" -Method POST -ContentType "application/json" -Body "{}"
```

**Expected output:** JSON with `confirmationCode`, `edifactContent`, and `format`

## 🎨 Frontend Setup

### Step 1: Open New Terminal

Keep the backend running and open a new PowerShell terminal.

### Step 2: Navigate to Frontend Directory

```powershell
cd F:\DEV\Java\PNRGOV\pnrgov-ui
```

### Step 3: Install Dependencies

```powershell
npm install
```

This will download all Angular dependencies (may take 2-3 minutes).

**Expected output:**
```
added XXX packages in XXs
```

### Step 4: Run Frontend

```powershell
npm start
```

**Expected output:**
```
** Angular Live Development Server is listening on localhost:4200 **
✔ Compiled successfully.
```

### Step 5: Access Application

Open browser: http://localhost:4200

You should see the PNR Gov home page with navigation menu.

## ✅ Verification Steps

### 1. Test EDIFACT Generator

1. Navigate to http://localhost:4200/generator
2. Click "Generate EDIFACT Message"
3. Verify success message appears
4. Verify EDIFACT content is displayed
5. Test "Copy to Clipboard" button
6. Test "Download" button

### 2. Test Manifest Generator

1. Navigate to http://localhost:4200/manifest
2. Click "Generate Flight Manifest"
3. Verify passenger table appears
4. Verify EDIFACT content is displayed
5. Test "Print Manifest" button
6. Test "Download EDIFACT" button

## 🔧 Troubleshooting

### Backend Issues

#### Port 8080 Already in Use

```powershell
# Find process using port 8080
netstat -ano | findstr :8080

# Kill the process (replace PID with actual process ID)
taskkill /PID <PID> /F
```

Or change port in `pnrgov-api/src/main/resources/application.properties`:

```properties
server.port=8081
```

Don't forget to update frontend environment:

```typescript
// pnrgov-ui/src/environments/environment.ts
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8081/api'
};
```

#### Build Failures

```powershell
# Clear Gradle cache
.\gradlew clean --refresh-dependencies

# Rebuild
.\gradlew build --info
```

#### Database Errors

Delete the database file and restart:

```powershell
Remove-Item -Path "pnrgov-api\pnrgov.db" -ErrorAction SilentlyContinue
.\gradlew :pnrgov-api:bootRun
```

### Frontend Issues

#### Port 4200 Already in Use

```powershell
# Find and kill process
netstat -ano | findstr :4200
taskkill /PID <PID> /F
```

Or use a different port:

```powershell
ng serve --port 4201
```

#### npm install Failures

```powershell
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
Remove-Item -Recurse -Force node_modules
Remove-Item package-lock.json
npm install
```

#### CORS Errors

Verify backend CORS configuration in `PnrGovApplication.java` includes `http://localhost:4200`.

If using a different port, update the CORS configuration:

```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:4200",
    "http://localhost:4201"  // Add your port
));
```

## 🔄 Restart Everything

```powershell
# Stop backend (Ctrl+C in backend terminal)
# Stop frontend (Ctrl+C in frontend terminal)

# Restart backend
cd F:\DEV\Java\PNRGOV
.\gradlew :pnrgov-api:bootRun

# Restart frontend (in new terminal)
cd F:\DEV\Java\PNRGOV\pnrgov-ui
npm start
```

## 📊 Development Workflow

### Hot Reload

- **Backend**: Changes require restart (`Ctrl+C`, then `.\gradlew :pnrgov-api:bootRun`)
- **Frontend**: Changes auto-reload (Angular CLI watches files)

### Building for Production

```powershell
# Backend JAR
.\gradlew :pnrgov-api:bootJar
# Output: pnrgov-api/build/libs/pnrgov-api.jar

# Frontend production build
cd pnrgov-ui
npm run build
# Output: pnrgov-ui/dist/pnrgov-ui/
```

## 🎯 Next Steps

- Review [README.md](README.md) for API documentation
- See [MIGRATION.md](MIGRATION.md) for C# to Java migration notes
- Explore OpenAPI documentation at http://localhost:8080/swagger-ui.html
- Check database with SQLite browser: `pnrgov-api/pnrgov.db`

## 📞 Support

If issues persist:

1. Check Java version: `java -version`
2. Check Node version: `node --version`
3. Review console logs for error messages
4. Clear all caches and rebuild from scratch
5. Contact development team

---

**Happy Coding!** 🚀
