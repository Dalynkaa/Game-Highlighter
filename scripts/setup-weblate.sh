#!/bin/bash

# Setup script for Weblate translation integration
# This script helps set up the translation workflow

set -e

# Configuration
WEBLATE_URL="${WEBLATE_URL:-https://weblate.nexbit.dev}"
WEBLATE_PROJECT="${WEBLATE_PROJECT:-highlighter}"
WEBLATE_COMPONENT="${WEBLATE_COMPONENT:-mod}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_step() {
    echo -e "${BLUE}[STEP]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if required tools are installed
check_requirements() {
    print_step "Checking requirements..."
    
    if ! command -v python3 &> /dev/null; then
        print_error "Python 3 is required but not installed"
        exit 1
    fi
    
    if ! python3 -c "import requests" &> /dev/null; then
        print_warning "Python 'requests' module not found. Installing..."
        pip3 install requests || {
            print_error "Failed to install requests module"
            exit 1
        }
    fi
    
    print_success "All requirements satisfied"
}

# Setup environment variables
setup_env() {
    print_step "Setting up environment..."
    
    if [ -z "$WEBLATE_TOKEN" ]; then
        echo -n "Enter your Weblate API token: "
        read -s WEBLATE_TOKEN
        echo
        
        echo "export WEBLATE_TOKEN='$WEBLATE_TOKEN'" >> ~/.bashrc
        print_success "API token saved to ~/.bashrc"
    fi
}

# Create Weblate component
create_component() {
    print_step "Creating Weblate component..."
    
    python3 /Users/dalynkaa/Documents/Develop/MineModding/Highlighter/scripts/weblate-upload.py \
        --url "$WEBLATE_URL" \
        --token "$WEBLATE_TOKEN" \
        --project "$WEBLATE_PROJECT" \
        --component "$WEBLATE_COMPONENT" \
        --create-component
    
    print_success "Component created successfully"
}

# Upload source translations
upload_source() {
    print_step "Uploading source translations..."
    
    python3 /Users/dalynkaa/Documents/Develop/MineModding/Highlighter/scripts/weblate-upload.py \
        --url "$WEBLATE_URL" \
        --token "$WEBLATE_TOKEN" \
        --project "$WEBLATE_PROJECT" \
        --component "$WEBLATE_COMPONENT"
    
    print_success "Source translations uploaded"
}

# Download existing translations
download_translations() {
    print_step "Downloading existing translations..."
    
    python3 scripts/weblate-download.py \
        --url "$WEBLATE_URL" \
        --token "$WEBLATE_TOKEN" \
        --project "$WEBLATE_PROJECT" \
        --component "$WEBLATE_COMPONENT" \
        --force
    
    print_success "Translations downloaded"
}

# Create GitHub workflow for automatic updates
create_github_workflow() {
    print_step "Creating GitHub workflow..."
    
    mkdir -p .github/workflows
    
    cat > .github/workflows/weblate-sync.yml << 'EOF'
name: Weblate Translation Sync

on:
  schedule:
    # Run daily at 2 AM UTC
    - cron: '0 2 * * *'
  workflow_dispatch:
  push:
    paths:
      - 'src/main/resources/assets/highlighter/lang/en_us.json'

jobs:
  sync-translations:
    runs-on: ubuntu-latest
    
    steps:
    - name: Checkout repository
      uses: actions/checkout@v4
    
    - name: Set up Python
      uses: actions/setup-python@v4
      with:
        python-version: '3.9'
    
    - name: Install dependencies
      run: |
        pip install requests
    
    - name: Upload source to Weblate
      run: |
        python3 scripts/weblate-upload.py \
          --url "${{ secrets.WEBLATE_URL }}" \
          --token "${{ secrets.WEBLATE_TOKEN }}" \
          --project "${{ vars.WEBLATE_PROJECT }}" \
          --component "${{ vars.WEBLATE_COMPONENT }}" \
          --update-git
    
    - name: Download translations from Weblate
      run: |
        python3 scripts/weblate-download.py \
          --url "${{ secrets.WEBLATE_URL }}" \
          --token "${{ secrets.WEBLATE_TOKEN }}" \
          --project "${{ vars.WEBLATE_PROJECT }}" \
          --component "${{ vars.WEBLATE_COMPONENT }}" \
          --force
    
    - name: Commit translation updates
      run: |
        git config --local user.email "action@github.com"
        git config --local user.name "GitHub Action"
        
        if [[ -n $(git status --porcelain) ]]; then
          git add src/main/resources/assets/highlighter/lang/
          git commit -m "Update translations from Weblate"
          git push
        else
          echo "No translation changes to commit"
        fi
      env:
        GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
EOF
    
    print_success "GitHub workflow created at .github/workflows/weblate-sync.yml"
}

# Main setup function
main() {
    echo -e "${BLUE}Highlighter Weblate Setup${NC}"
    echo "=========================="
    echo
    
    check_requirements
    setup_env
    
    echo
    echo "What would you like to do?"
    echo "1. Create Weblate component"
    echo "2. Upload source translations"
    echo "3. Download existing translations"
    echo "4. Create GitHub workflow"
    echo "5. Full setup (all of the above)"
    echo -n "Choose an option (1-5): "
    
    read -r option
    
    case $option in
        1)
            create_component
            ;;
        2)
            upload_source
            ;;
        3)
            download_translations
            ;;
        4)
            create_github_workflow
            ;;
        5)
            create_component
            upload_source
            download_translations
            create_github_workflow
            ;;
        *)
            print_error "Invalid option"
            exit 1
            ;;
    esac
    
    echo
    print_success "Setup completed successfully!"
    echo
    echo "Next steps:"
    echo "1. Configure your Weblate instance with the created component"
    echo "2. Set up GitHub secrets: WEBLATE_URL, WEBLATE_TOKEN"
    echo "3. Set up GitHub variables: WEBLATE_PROJECT, WEBLATE_COMPONENT"
    echo "4. Start translating at: $WEBLATE_URL/projects/$WEBLATE_PROJECT/$WEBLATE_COMPONENT/"
}

main "$@"