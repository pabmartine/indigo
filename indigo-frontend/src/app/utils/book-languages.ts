export function normalizeBookLanguages(languages: string[] | undefined): string[] {
  const aliases: Record<string, string> = {
    spa: 'es', eng: 'en', fra: 'fr', deu: 'de', ita: 'it', por: 'pt', swe: 'sv', cat: 'ca'
  };
  return (languages && languages.length > 0 ? languages : ['en']).map(language => {
    const normalized = language.toLowerCase().split(/[-_]/)[0];
    return aliases[normalized] || normalized;
  });
}
