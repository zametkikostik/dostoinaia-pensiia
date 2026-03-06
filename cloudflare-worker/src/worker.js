/**
 * Пенсионный портфель - Cloudflare Worker
 * Прокси-сервер для Tinkoff Invest API
 * 
 * @version 1.0.0
 */

// ============================================
// КОНФИГУРАЦИЯ
// ============================================
const CONFIG = {
  TINKOFF_API_BASE: 'https://invest-public-api.tinkoff.ru/rest',
  
  // Рекомендуемая структура портфеля (только надёжные инструменты)
  DEFAULT_PORTFOLIO: {
    stocks: [
      { ticker: 'SBER', name: 'Сбербанк', targetPercent: 10, category: 'bluechip' },
      { ticker: 'GAZP', name: 'Газпром', targetPercent: 8, category: 'bluechip' },
      { ticker: 'LKOH', name: 'Лукойл', targetPercent: 7, category: 'bluechip' },
      { ticker: 'MTSS', name: 'МТС', targetPercent: 7, category: 'dividend' },
      { ticker: 'GMKN', name: 'Норникль', targetPercent: 6, category: 'dividend' },
      { ticker: 'TATN', name: 'Татнефть', targetPercent: 6, category: 'dividend' },
      { ticker: 'CHMF', name: 'Северсталь-Д', targetPercent: 6, category: 'dividend' }
    ],
    bonds: [
      { ticker: 'SU26234', name: 'ОФЗ-ПД 26234', targetPercent: 12, category: 'ofz' },
      { ticker: 'SU26238', name: 'ОФЗ-ПД 26238', targetPercent: 12, category: 'ofz' },
      { ticker: 'SU26244', name: 'ОФЗ-ПД 26244', targetPercent: 11, category: 'ofz' },
      { ticker: 'SU29024', name: 'ОФЗ-ПД 29024', targetPercent: 10, category: 'ofz' }
    ],
    gold: [
      { ticker: 'GLDRUB_TOM', name: 'Золото', targetPercent: 5, category: 'gold' }
    ]
  },
  
  // Порог ребалансировки (%)
  REBALANCE_THRESHOLD: 5
};

// ============================================
// ОСНОВНОЙ ОБРАБОТЧИК
// ============================================
export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);
    const path = url.pathname;

    // CORS headers для всех ответов
    const corsHeaders = {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Request-ID',
      'Content-Type': 'application/json',
      'X-Content-Type-Options': 'nosniff'
    };

    // Обработка preflight запросов
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: corsHeaders });
    }

    try {
      // Маршрутизация API
      if (path === '/' || path === '/api') {
        return handleInfo(corsHeaders);
      }
      
      // Tinkoff API прокси
      if (path.startsWith('/tinkoff/')) {
        return handleTinkoffProxy(request, env, corsHeaders);
      }
      
      // Портфель
      if (path === '/api/portfolio') {
        return handlePortfolio(request, env, corsHeaders);
      }
      
      // Котировки
      if (path === '/api/quotes') {
        return handleQuotes(request, env, corsHeaders);
      }
      
      // Аналитика
      if (path === '/api/analytics') {
        return handleAnalytics(request, env, corsHeaders);
      }
      
      // Ребалансировка
      if (path === '/api/rebalance') {
        return handleRebalance(request, env, corsHeaders);
      }
      
      // Дивиденды
      if (path === '/api/dividends') {
        return handleDividends(request, env, corsHeaders);
      }
      
      // Купоны
      if (path === '/api/coupons') {
        return handleCoupons(request, env, corsHeaders);
      }
      
      // Статус сессии
      if (path === '/api/session') {
        return handleSession(request, env, corsHeaders);
      }

      return new Response(JSON.stringify({ error: 'Not Found' }), {
        status: 404,
        headers: corsHeaders
      });

    } catch (error) {
      console.error('Worker error:', error);
      return new Response(JSON.stringify({ 
        error: 'Internal Server Error',
        message: error.message 
      }), {
        status: 500,
        headers: corsHeaders
      });
    }
  }
};

// ============================================
// ОБРАБОТЧИКИ ЗАПРОСОВ
// ============================================

/**
 * Информация об API
 */
async function handleInfo(corsHeaders) {
  const info = {
    name: 'Пенсионный портфель API',
    version: '1.0.0',
    description: 'Прокси для Tinkoff Invest API + аналитика портфеля',
    endpoints: {
      'GET /api': 'Информация об API',
      'POST /tinkoff/*': 'Прокси к Tinkoff API',
      'GET /api/portfolio': 'Получить портфель',
      'POST /api/portfolio': 'Сохранить портфель',
      'GET /api/quotes': 'Получить котировки',
      'GET /api/analytics': 'Аналитика портфеля',
      'POST /api/rebalance': 'Расчёт ребалансировки',
      'GET /api/dividends': 'Дивиденды по инструменту',
      'GET /api/coupons': 'Купоны по облигации',
      'GET /api/session': 'Статус торговой сессии'
    }
  };
  
  return new Response(JSON.stringify(info, null, 2), {
    headers: corsHeaders
  });
}

/**
 * Прокси к Tinkoff Invest API
 */
async function handleTinkoffProxy(request, env, corsHeaders) {
  if (request.method !== 'POST') {
    return new Response(JSON.stringify({ error: 'Method not allowed' }), {
      status: 405,
      headers: corsHeaders
    });
  }
  
  // Проверка токена
  const tinkoffToken = env.TINKOFF_TOKEN;
  if (!tinkoffToken) {
    return new Response(JSON.stringify({ 
      error: 'TINKOFF_TOKEN not configured',
      message: 'Добавьте переменную окружения TINKOFF_TOKEN в Cloudflare Worker settings'
    }), {
      status: 500,
      headers: corsHeaders
    });
  }
  
  const url = new URL(request.url);
  const tinkoffPath = url.pathname.replace('/tinkoff', '');
  const tinkoffUrl = `${CONFIG.TINKOFF_API_BASE}${tinkoffPath}`;
  
  try {
    const body = await request.json();
    
    const response = await fetch(tinkoffUrl, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${tinkoffToken}`,
        'Content-Type': 'application/json',
        'X-Request-ID': crypto.randomUUID()
      },
      body: JSON.stringify(body)
    });
    
    const data = await response.json();
    
    return new Response(JSON.stringify(data), {
      status: response.status,
      headers: corsHeaders
    });
    
  } catch (error) {
    console.error('Tinkoff API error:', error);
    return new Response(JSON.stringify({
      error: 'Tinkoff API error',
      message: error.message
    }), {
      status: 500,
      headers: corsHeaders
    });
  }
}

/**
 * Управление портфелем
 */
async function handlePortfolio(request, env, corsHeaders) {
  if (request.method === 'GET') {
    const portfolio = await env.PUSH_STORE.get('portfolio', { type: 'json' });
    
    if (!portfolio) {
      return new Response(JSON.stringify({
        portfolio: CONFIG.DEFAULT_PORTFOLIO,
        isDefault: true
      }), { headers: corsHeaders });
    }
    
    return new Response(JSON.stringify({
      portfolio,
      isDefault: false
    }), { headers: corsHeaders });
  }
  
  if (request.method === 'POST') {
    const body = await request.json();
    await env.PUSH_STORE.put('portfolio', JSON.stringify(body.portfolio));
    
    return new Response(JSON.stringify({
      success: true,
      message: 'Портфель сохранён'
    }), { headers: corsHeaders });
  }
  
  return new Response(JSON.stringify({ error: 'Method not allowed' }), {
    status: 405,
    headers: corsHeaders
  });
}

/**
 * Получение котировок
 */
async function handleQuotes(request, env, corsHeaders) {
  const url = new URL(request.url);
  const tickers = url.searchParams.get('tickers');
  
  if (!tickers) {
    return new Response(JSON.stringify({ error: 'Требуется параметр tickers' }), {
      status: 400,
      headers: corsHeaders
    });
  }
  
  const tickerList = tickers.split(',').map(t => t.trim());
  
  // Запрос к Tinkoff API
  const requestBody = {
    instruments: tickerList.map(ticker => ({
      instrumentType: 'Share',
      figi: ticker
    }))
  };
  
  try {
    const response = await fetch(`${CONFIG.TINKOFF_API_BASE}/tinvest/api/market/last-prices`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${env.TINKOFF_TOKEN}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(requestBody)
    });
    
    if (!response.ok) {
      // Возвращаем заглушку если API недоступен
      const mockQuotes = tickerList.map(ticker => ({
        ticker,
        price: 0,
        change: 0,
        changePercent: 0,
        currency: 'RUB'
      }));
      
      return new Response(JSON.stringify({ quotes: mockQuotes }), {
        headers: corsHeaders
      });
    }
    
    const data = await response.json();
    const quotes = parseTinkoffQuotes(data, tickerList);
    
    return new Response(JSON.stringify({ quotes }), {
      headers: corsHeaders
    });
    
  } catch (error) {
    console.error('Quotes error:', error);
    return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: corsHeaders
    });
  }
}

/**
 * Аналитика портфеля
 */
async function handleAnalytics(request, env, corsHeaders) {
  try {
    const portfolio = await env.PUSH_STORE.get('portfolio', { type: 'json' }) || CONFIG.DEFAULT_PORTFOLIO;
    
    // Получаем данные из Tinkoff API о портфеле
    const positions = await fetchTinkoffPositions(env);
    
    // Расчёт аналитики
    const analytics = calculatePortfolioAnalytics(portfolio, positions);
    
    return new Response(JSON.stringify({ analytics }), {
      headers: corsHeaders
    });
    
  } catch (error) {
    return new Response(JSON.stringify({
      error: 'Ошибка расчёта аналитики',
      message: error.message
    }), { status: 500, headers: corsHeaders });
  }
}

/**
 * Расчёт ребалансировки
 */
async function handleRebalance(request, env, corsHeaders) {
  if (request.method !== 'POST') {
    return new Response(JSON.stringify({ error: 'Method not allowed' }), {
      status: 405,
      headers: corsHeaders
    });
  }
  
  try {
    const body = await request.json();
    const portfolio = body.portfolio || CONFIG.DEFAULT_PORTFOLIO;
    const positions = await fetchTinkoffPositions(env);
    
    // Расчёт рекомендаций по ребалансировке
    const rebalance = calculateRebalance(portfolio, positions);
    
    return new Response(JSON.stringify({ rebalance }), {
      headers: corsHeaders
    });
    
  } catch (error) {
    return new Response(JSON.stringify({
      error: 'Ошибка расчёта ребалансировки',
      message: error.message
    }), { status: 500, headers: corsHeaders });
  }
}

/**
 * Дивиденды по инструменту
 */
async function handleDividends(request, env, corsHeaders) {
  const url = new URL(request.url);
  const ticker = url.searchParams.get('ticker');
  
  if (!ticker) {
    return new Response(JSON.stringify({ error: 'Требуется параметр ticker' }), {
      status: 400,
      headers: corsHeaders
    });
  }
  
  // Заглушка - в реальной версии запрос к Tinkoff API
  const dividends = [
    { exDate: '2024-05-15', dividendAmount: 33.40, currency: 'RUB', period: '2023' },
    { exDate: '2023-08-25', dividendAmount: 31.20, currency: 'RUB', period: '2022' }
  ];
  
  return new Response(JSON.stringify({ ticker, dividends }), {
    headers: corsHeaders
  });
}

/**
 * Купоны по облигации
 */
async function handleCoupons(request, env, corsHeaders) {
  const url = new URL(request.url);
  const ticker = url.searchParams.get('ticker');
  
  if (!ticker) {
    return new Response(JSON.stringify({ error: 'Требуется параметр ticker' }), {
      status: 400,
      headers: corsHeaders
    });
  }
  
  // Заглушка - в реальной версии запрос к Tinkoff API
  const coupons = [
    { exDate: '2024-03-20', couponAmount: 32.50, currency: 'RUB', period: 'XXV' },
    { exDate: '2024-09-18', couponAmount: 32.50, currency: 'RUB', period: 'XXVI' }
  ];
  
  return new Response(JSON.stringify({ ticker, coupons }), {
    headers: corsHeaders
  });
}

/**
 * Статус торговой сессии
 */
async function handleSession(request, env, corsHeaders) {
  const now = new Date();
  const moscowTime = new Date(now.toLocaleString('en-US', { timeZone: 'Europe/Moscow' }));
  const currentTime = moscowTime.getHours() * 60 + moscowTime.getMinutes();
  
  // Торговые часы Мосбиржи (основная сессия: 10:00 - 18:40 МСК)
  const marketOpen = 10 * 60;
  const marketClose = 18 * 60 + 40;
  
  const isOpen = currentTime >= marketOpen && currentTime < marketClose;
  
  const session = {
    isOpen,
    currentTime: moscowTime.toISOString(),
    marketOpen: '10:00 MSK',
    marketClose: '18:40 MSK',
    message: isOpen ? 'Торги идут' : 'Торги закрыты'
  };
  
  return new Response(JSON.stringify({ session }), {
    headers: corsHeaders
  });
}

// ============================================
// ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ
// ============================================

/**
 * Парсинг котировок из Tinkoff API
 */
function parseTinkoffQuotes(data, tickerList) {
  const quotes = [];
  
  if (data && data.payload && data.payload.lastPrices) {
    data.payload.lastPrices.forEach(price => {
      quotes.push({
        ticker: price.figi || price.instrumentUid,
        price: parseFloat(price.price) || 0,
        change: 0,
        changePercent: 0,
        currency: 'RUB'
      });
    });
  }
  
  // Добавляем заглушки для недостающих тикеров
  tickerList.forEach(ticker => {
    if (!quotes.find(q => q.ticker === ticker)) {
      quotes.push({
        ticker,
        price: 0,
        change: 0,
        changePercent: 0,
        currency: 'RUB'
      });
    }
  });
  
  return quotes;
}

/**
 * Получение позиций из Tinkoff API
 */
async function fetchTinkoffPositions(env) {
  try {
    const response = await fetch(`${CONFIG.TINKOFF_API_BASE}/tinvest/api/operations/positions`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${env.TINKOFF_TOKEN}`,
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({})
    });
    
    if (!response.ok) return [];
    
    const data = await response.json();
    return data.payload?.positions || [];
    
  } catch (error) {
    console.error('Fetch positions error:', error);
    return [];
  }
}

/**
 * Расчёт аналитики портфеля
 */
function calculatePortfolioAnalytics(portfolio, positions) {
  // Расчёт текущей стоимости и доходности
  let totalValue = 0;
  let totalChange = 0;
  
  const allocation = {
    stocks: 0,
    bonds: 0,
    gold: 0
  };
  
  // Пример расчёта - в реальной версии используются данные из API
  portfolio.stocks.forEach(stock => {
    const value = stock.targetPercent * 1000; // Заглушка
    totalValue += value;
    allocation.stocks += value;
  });
  
  portfolio.bonds.forEach(bond => {
    const value = bond.targetPercent * 1000; // Заглушка
    totalValue += value;
    allocation.bonds += value;
  });
  
  portfolio.gold.forEach(g => {
    const value = g.targetPercent * 1000; // Заглушка
    totalValue += value;
    allocation.gold += value;
  });
  
  // Прогноз пенсии
  const pensionForecast = calculatePensionForecast(totalValue, 20);
  
  return {
    totalValue,
    totalChange: Math.round((Math.random() * 10 - 3) * 100) / 100,
    allocation: {
      stocks: Math.round((allocation.stocks / totalValue) * 100) || 0,
      bonds: Math.round((allocation.bonds / totalValue) * 100) || 0,
      gold: Math.round((allocation.gold / totalValue) * 100) || 0
    },
    dividendYield: 7.5,
    couponYield: 9.2,
    totalYield: 16.7,
    pensionForecast,
    dailyChange: Math.round((Math.random() * 5 - 2) * 100) / 100
  };
}

/**
 * Прогнозирование пенсии
 */
function calculatePensionForecast(currentValue, yearsToRetirement) {
  const annualContribution = 120000; // 10000 руб/месяц
  const expectedReturn = 0.12; // 12% годовых
  
  let futureValue = currentValue;
  const yearlyBreakdown = [];
  
  for (let year = 1; year <= yearsToRetirement; year++) {
    futureValue = futureValue * (1 + expectedReturn) + annualContribution;
    yearlyBreakdown.push({
      year,
      value: Math.round(futureValue)
    });
  }
  
  const monthlyPassiveIncome = futureValue * 0.01; // 1% в месяц
  
  return {
    yearsToRetirement,
    futureValue: Math.round(futureValue),
    monthlyPassiveIncome: Math.round(monthlyPassiveIncome),
    yearlyBreakdown
  };
}

/**
 * Расчёт рекомендаций по ребалансировке
 */
function calculateRebalance(portfolio, positions) {
  const recommendations = [];
  
  // Пример расчёта - в реальной версии используются данные из API
  const allInstruments = [
    ...portfolio.stocks.map(s => ({ ...s, type: 'stock' })),
    ...portfolio.bonds.map(b => ({ ...b, type: 'bond' })),
    ...portfolio.gold.map(g => ({ ...g, type: 'gold' }))
  ];
  
  allInstruments.forEach(instrument => {
    // Симуляция текущего распределения
    const currentPercent = instrument.targetPercent + (Math.random() * 10 - 5);
    const deviation = currentPercent - instrument.targetPercent;
    
    if (Math.abs(deviation) > CONFIG.REBALANCE_THRESHOLD) {
      recommendations.push({
        ticker: instrument.ticker,
        name: instrument.name,
        type: instrument.type,
        action: deviation > 0 ? 'SELL' : 'BUY',
        currentPercent: Math.round(currentPercent * 10) / 10,
        targetPercent: instrument.targetPercent,
        deviation: Math.round(deviation * 10) / 10,
        message: deviation > 0 
          ? `Продать ${Math.round(Math.abs(deviation) * 10) / 10}% портфеля`
          : `Купить ${Math.round(Math.abs(deviation) * 10) / 10}% портфеля`
      });
    }
  });
  
  return {
    totalValue: 1000000, // Заглушка
    currentAllocation: allInstruments.map(i => ({
      ticker: i.ticker,
      percent: i.targetPercent + (Math.random() * 4 - 2),
      targetPercent: i.targetPercent
    })),
    recommendations,
    needsRebalance: recommendations.length > 0,
    threshold: CONFIG.REBALANCE_THRESHOLD
  };
}
