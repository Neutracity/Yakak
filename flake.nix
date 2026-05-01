{
  description = "Pinko dev env";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs, ... }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
      };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          pkgs.jdk17
        ];

        shellHook = ''
          export JAVA_HOME=${pkgs.jdk17}
        '';
      };
    };
}
