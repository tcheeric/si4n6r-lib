
# si4n6r-storage-fs

A simple file system storage for si4n6r.
We store the user's nsec in the vault, it is mapped to the user's nip-05 identifier. The nsec will be used for signing events submitted by the user's app.
The nsec is stored encrypted in a file, and only the user, who is the only one in possession of the password, can decrypt it.
The user submits the password to the vault, not via the application, but directly via the signer's UI (shibboleth), and the nsec is decrypted for the duration of the session.

This is a very simple implementation serving as a proof of concept, and is not intended for production use.

## vault file structure
- `<base dir>`: The base directory (by default, `$HOME/.si4n6r`)
- `<base dir>/secrets`: Folder containing the PEM-encoded private keys used for securing the nsecs.
- `<base dir>/account`: Folder containing the different user accounts
    -  `<base dir>/account/<nip05>`: Folder containing the corresponding nsec for the <nip05>. The nsec is encrypted using the private key in <base dir>/secrets/<npub>.pem
    - `<base dir>/account/<nip05>/applications`: The folder containing the different applications registered by the user.
    -  `<base dir>/account/<nip05>/applications/hash(<npuba>)`: A symbolic link to the application `<npuba>` (see below)
- `<base dir>/application`: Folder containing the different applications registered by the users.
    - `<base dir>/application/hash(<npuba>)/metadata.json`: The application metadata file.
