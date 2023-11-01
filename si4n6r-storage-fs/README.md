
# si4n6r-storage-fs

A simple file system based storage for si4n6r.
We store the user's nsec in the vault, it is mapped to the user's registered app and will be used for signing events submitted by this app.
The nsec is stored in an encrypted file, and only the user is in possession of the password to decrypt it.
The user submits the password via the UI, and the vault is unlocked for the duration of the session.

This is a very simple implementation serving as a proof of concept, and is not intended for production use.

## vault file structure
- `<base dir>`: The base directory (`$HOME/.si4n6r`)
- `<base dir>/secrets`: Folder containing the PEM-encoded private keys used for securing the nsecs.
- `<base dir>/account`: Folder containing the different user accounts
    -  `<base dir>/account/<npub>`: Folder containing the corresponding nsec for the <npub>. The nsec is encrypted using the private key in <base dir>/secrets/<npub>.pem
    - `<base dir>/account/<npub>/applications`: The folder containing the different applications registered by the user.
    -  `<base dir>/account/<npub>/applications/<npuba>`: A symbolic link to the application `<npuba>` (see below)
- `<base dir>/application`: Folder containing the different applications registered by the users.
    - `<base dir>/application/metadata.json`: The application metadata file.
