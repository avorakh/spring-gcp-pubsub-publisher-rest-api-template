# spring-gcp-pubsub-publisher-rest-api-template

spring-gcp-pubsub-publisher-rest-api-template

## App Info API
```bash
curl -v 'http://localhost:8090/template/app/info'
```

## Google Cloud CLI Configuration with a Service Account Key

This guide explains how to configure the **Google Cloud CLI (gcloud)** with a **service account key** for **macOS**, **Linux (Ubuntu)**, and **Windows (PowerShell)**. The environment variables will be saved in profile files so they persist across sessions.

---

### 0. Configure the Google Cloud Project

#### 0.1. Create the Pub/Sub Topic (Console, not gcloud)

1. Go to **Pub/Sub → Topics**.
2. Click **Create Topic**.
3. Enter a topic name, for example:

```
projects/sample-app-123456/topics/my-topic
```

4. Click **Create**.

Make note of the topic ID (e.g., `my-topic`).

---

#### 0.2. Grant the Service Account Permission **Only on the Created Topic**

> **This step is done in the Google Cloud Console (NOT using gcloud).**  
> You will grant the service account **Pub/Sub Publisher** access *scoped to this single topic*, not the entire project.

1. Go to **Pub/Sub → Topics**.
2. Click the topic you created earlier (e.g., `my-topic`).
3. Click the **Permissions** tab.
4. Click **Add Principal**.
5. Enter your service account email  
   (e.g., `my-service-account@sample-app-123456.iam.gserviceaccount.com`).
6. Add the following role:

| Resource          | Role                                   |
|-------------------|----------------------------------------|
| This topic only   | **Pub/Sub Publisher** (`roles/pubsub.publisher`) |

7. Save the changes.

This ensures the service account can publish **only to this specific topic**.

---

### 1. Create a Secure Directory for Your Service Account Key

#### macOS / Linux (Ubuntu)

```bash
mkdir -p $HOME/.security
mv my-service-account-key.json $HOME/.security/
```

#### Windows (PowerShell)

```powershell
New-Item -ItemType Directory -Path "$env:USERPROFILE\.security" -Force
Move-Item .\my-service-account-key.json "$env:USERPROFILE\.security\"
```

---

### 2. Set Environment Variables (Persistent)

#### macOS / Linux (Ubuntu)

1. Open your shell profile file (`~/.bashrc` or `~/.zshrc`):

```bash
nano ~/.bashrc   # or ~/.zshrc for zsh
```

2. Add the following lines:

```bash
# Google Cloud SDK
export CLOUDSDK_PYTHON=python3
export GOOGLE_CLOUD_PROJECT="sample-app-123456"
export GOOGLE_APPLICATION_CREDENTIALS="$HOME/.security/my-service-account-key.json"
```

3. Save the file and reload the profile:

```bash
source ~/.bashrc   # or source ~/.zshrc
```

---

#### Windows (PowerShell)

1. Open your PowerShell profile:

```powershell
notepad $PROFILE
```

> If `$PROFILE` does not exist, create it:

```powershell
New-Item -ItemType File -Path $PROFILE -Force
```

2. Add the following lines:

```powershell
# Google Cloud SDK
$env:CLOUDSDK_PYTHON = "python3"
$env:GOOGLE_CLOUD_PROJECT = "sample-app-123456"
$env:GOOGLE_APPLICATION_CREDENTIALS = "$env:USERPROFILE\.security\my-service-account-key.json"
```

3. Save the file and reload the profile:

```powershell
. $PROFILE
```

---

### 3. Verify Application Default Credentials (ADC)

```bash
gcloud auth application-default print-access-token
gcloud auth list
```

> If a token prints successfully, ADC is properly configured.  
> ADC is used by SDKs (Python, Java, Node.js, Go, etc.) and Terraform.

---


## How to run the application with Google Cloud Pub/Sub simulator
### Setup
TBA

### How to run the application

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Publish Event API

```bash
curl -v -X POST http://localhost:8090/template/events -H "Content-Type: application/json" -d '{"type":"order.created","payload":"{\"orderId\":123}"}'
```