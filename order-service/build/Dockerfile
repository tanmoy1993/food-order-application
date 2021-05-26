FROM mcr.microsoft.com/dotnet/core/aspnet:3.1

COPY App/publish/ App/
WORKDIR /App
ENTRYPOINT ["dotnet", "WebApplication2.dll"]
