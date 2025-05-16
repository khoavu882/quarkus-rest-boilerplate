#!/bin/bash

# Print current working directory for debugging
echo "Current working directory: $(pwd)"
echo "Directory contents:"
ls -la

# Load environment variables from .env file
export $(grep -v '^#' .env | xargs)

# Execute the command passed to the script
exec "$@"
