#!/usr/bin/env python3
"""
Weblate Translation Upload Script
Uploads updated language files to self-hosted Weblate instance.
"""

import os
import sys
import json
import requests
import argparse
from pathlib import Path
from typing import Dict, List

class WeblateUploader:
    def __init__(self, base_url: str, api_token: str, project: str, component: str):
        self.base_url = base_url.rstrip('/')
        self.api_token = api_token
        self.project = project
        self.component = component
        self.session = requests.Session()
        self.session.headers.update({
            'Authorization': f'Token {api_token}',
        })
    
    def upload_source_file(self, source_file: Path) -> bool:
        """Upload source language file (en_us.json) to Weblate."""
        if not source_file.exists():
            print(f"Source file not found: {source_file}")
            return False
        
        url = f"{self.base_url}/api/components/{self.project}/{self.component}/translations/en/file/"
        
        try:
            with open(source_file, 'rb') as f:
                files = {'file': ('en_us.json', f, 'application/json')}
                data = {'method': 'replace'}
                
                response = self.session.post(url, files=files, data=data)
                response.raise_for_status()
                
                print(f"✓ Successfully uploaded source file: {source_file.name}")
                return True
                
        except requests.RequestException as e:
            print(f"✗ Error uploading source file: {e}")
            return False
    
    def create_component_if_not_exists(self) -> bool:
        """Create Weblate component if it doesn't exist."""
        url = f"{self.base_url}/api/projects/{self.project}/components/"
        
        # Check if component exists
        try:
            check_url = f"{self.base_url}/api/components/{self.project}/{self.component}/"
            response = self.session.get(check_url)
            if response.status_code == 200:
                print(f"Component {self.component} already exists")
                return True
        except requests.RequestException:
            pass
        
        # Create component
        component_data = {
            "name": "Highlighter Mod",
            "slug": self.component,
            "vcs": "git",
            "repo": f"https://github.com/dalynkaa/Highlighter.git",  # Замени на свой репозиторий
            "filemask": "src/main/resources/assets/highlighter/lang/*.json",
            "template": "src/main/resources/assets/highlighter/lang/en_us.json",
            "file_format": "json",
            "license": "MIT",
            "agreement": "",
            "new_lang": "add",
            "language_code_style": "posix"
        }
        
        try:
            response = self.session.post(url, json=component_data)
            response.raise_for_status()
            print(f"✓ Created component: {self.component}")
            return True
            
        except requests.RequestException as e:
            print(f"✗ Error creating component: {e}")
            if hasattr(e, 'response') and e.response is not None:
                print(f"Response: {e.response.text}")
            return False
    
    def update_from_git(self) -> bool:
        """Update Weblate component from git repository."""
        url = f"{self.base_url}/api/components/{self.project}/{self.component}/repository/"
        
        try:
            data = {"operation": "pull"}
            response = self.session.post(url, json=data)
            response.raise_for_status()
            
            print("✓ Updated component from git repository")
            return True
            
        except requests.RequestException as e:
            print(f"✗ Error updating from git: {e}")
            return False
    
    def get_component_stats(self) -> Dict:
        """Get component translation statistics."""
        url = f"{self.base_url}/api/components/{self.project}/{self.component}/statistics/"
        
        try:
            response = self.session.get(url)
            response.raise_for_status()
            return response.json()
            
        except requests.RequestException as e:
            print(f"✗ Error getting stats: {e}")
            return {}

def main():
    parser = argparse.ArgumentParser(description='Upload translations to Weblate')
    parser.add_argument('--url', required=True, help='Weblate base URL')
    parser.add_argument('--token', required=True, help='Weblate API token')
    parser.add_argument('--project', default='highlighter', help='Weblate project name')
    parser.add_argument('--component', default='mod', help='Weblate component name')
    parser.add_argument('--lang-dir', default='src/main/resources/assets/highlighter/lang',
                       help='Language files directory')
    parser.add_argument('--create-component', action='store_true', 
                       help='Create Weblate component if it doesn\'t exist')
    parser.add_argument('--update-git', action='store_true',
                       help='Update component from git repository')
    
    args = parser.parse_args()
    
    # Resolve language directory path
    script_dir = Path(__file__).parent
    project_root = script_dir.parent
    lang_dir = project_root / args.lang_dir
    
    print(f"Highlighter Translation Uploader")
    print(f"Weblate URL: {args.url}")
    print(f"Project: {args.project}")
    print(f"Component: {args.component}")
    print(f"Language directory: {lang_dir}")
    print("-" * 50)
    
    uploader = WeblateUploader(args.url, args.token, args.project, args.component)
    
    # Create component if requested
    if args.create_component:
        if not uploader.create_component_if_not_exists():
            sys.exit(1)
    
    # Update from git if requested
    if args.update_git:
        uploader.update_from_git()
    
    # Upload source file
    source_file = lang_dir / "en_us.json"
    if source_file.exists():
        uploader.upload_source_file(source_file)
    else:
        print(f"Source file not found: {source_file}")
    
    # Show statistics
    stats = uploader.get_component_stats()
    if stats:
        print("\nTranslation Statistics:")
        for stat in stats:
            if 'name' in stat and 'translated_percent' in stat:
                print(f"  {stat['name']}: {stat['translated_percent']:.1f}% translated")
    
    print("\nUpload completed!")

if __name__ == '__main__':
    main()