# Vercel Blob Storage Setup

## Environment Variable Required

Add this to your Vercel project environment variables:

```
BLOB_READ_WRITE_TOKEN=your_token_here
```

## How to Get the Token

1. Go to your Vercel dashboard: https://vercel.com/dashboard
2. Navigate to your project **ausrufezeichen-geh-lter**
3. Go to **Storage** tab
4. Click **Create Database** → Select **Blob**
5. Follow the setup wizard
6. Copy the **BLOB_READ_WRITE_TOKEN** from the environment variables
7. It will be automatically added to your project

## Important Notes

- The token is automatically injected into your serverless functions
- No need to add it to `.env.local` for local development (it will fallback gracefully)
- Images are stored with public access
- URLs are permanent and delivered via CDN

## Deployment

After adding the Blob store:

```bash
cd backend
npm install
git add .
git commit -m "Add Vercel Blob Storage for image uploads"
git push
```

Vercel will automatically detect the Blob storage and configure it.
