# Deploying Documentation to GitHub Pages

This guide explains how to deploy the documentation to GitHub Pages.

## Prerequisites

- GitHub repository
- GitHub Pages enabled on your repository

## Setup Instructions

### 1. Push Your Code to GitHub

```bash
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/yourusername/ai-notification-platform.git
git push -u origin main
```

### 2. Enable GitHub Pages

1. Go to your repository on GitHub
2. Click on **Settings**
3. Scroll down to **Pages** in the left sidebar
4. Under **Source**, select **GitHub Actions**

### 3. Configure the Documentation

Update the `_config.yml` file with your repository details:

```yaml
url: https://yourusername.github.io
baseurl: /ai-notification-platform

aux_links:
  "View on GitHub":
    - "//github.com/yourusername/ai-notification-platform"
```

Replace `yourusername` with your actual GitHub username.

### 4. Push Documentation Changes

```bash
git add docs/
git commit -m "Add documentation"
git push origin main
```

### 5. Deploy

The GitHub Action will automatically:
1. Build the Jekyll site
2. Deploy to GitHub Pages
3. Make it available at `https://yourusername.github.io/ai-notification-platform/`

You can monitor the deployment progress:
1. Go to your repository on GitHub
2. Click on **Actions** tab
3. Watch the "Deploy Documentation to GitHub Pages" workflow

## Local Testing

To test the documentation locally before deploying:

### Install Ruby and Bundler

**macOS:**
```bash
brew install ruby
gem install bundler
```

**Linux:**
```bash
sudo apt install ruby-full build-essential
gem install bundler
```

### Run Jekyll Locally

```bash
cd docs
bundle install
bundle exec jekyll serve
```

Open http://localhost:4000/ai-notification-platform/ in your browser.

## Manual Deployment

If you prefer manual deployment instead of GitHub Actions:

1. Go to **Settings** → **Pages**
2. Under **Source**, select **Deploy from a branch**
3. Select branch: `main`
4. Select folder: `/docs`
5. Click **Save**

Note: With manual deployment, you won't need the `.github/workflows/deploy-docs.yml` file.

## Updating Documentation

To update the documentation:

1. Edit files in the `docs/` directory
2. Test locally (optional): `bundle exec jekyll serve`
3. Commit and push:
   ```bash
   git add docs/
   git commit -m "Update documentation"
   git push origin main
   ```
4. GitHub Actions will automatically rebuild and deploy

## Troubleshooting

### Build Fails

Check the Actions tab for error messages. Common issues:

- **Syntax errors in YAML**: Validate `_config.yml` syntax
- **Missing dependencies**: Ensure `Gemfile` includes all required gems
- **Broken links**: Check all internal links in markdown files

### Pages Not Updating

- Wait 2-3 minutes after push for deployment
- Clear browser cache
- Check Actions tab for deployment status
- Verify GitHub Pages is enabled in Settings

### Custom Domain

To use a custom domain:

1. Add a `CNAME` file in the `docs/` directory with your domain
2. Update DNS settings with your domain provider
3. Configure custom domain in GitHub Pages settings

## Resources

- [GitHub Pages Documentation](https://docs.github.com/en/pages)
- [Jekyll Documentation](https://jekyllrb.com/docs/)
- [Just the Docs Theme](https://just-the-docs.github.io/just-the-docs/)
