/* ==========================================================================
   VKLP TERMINAL — client runtime
   Identical code runs against the local Spring Boot preview and the static
   GitHub Pages build: both serve ./data/*.json, and neither is required —
   if the fetch fails (e.g. opening docs/index.html straight off the disk)
   the page falls back to the quote already rendered into the markup.
   ========================================================================== */
(() => {
    'use strict';

    const $ = (sel, root = document) => root.querySelector(sel);
    const $$ = (sel, root = document) => Array.from(root.querySelectorAll(sel));
    const reduceMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    /* ── seeded PRNG so the tape is deterministic per load ─────────────────── */

    function mulberry32(seed) {
        let a = seed >>> 0;
        return () => {
            a = (a + 0x6D2B79F5) >>> 0;
            let t = a;
            t = Math.imul(t ^ (t >>> 15), t | 1);
            t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
            return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
        };
    }

    /** Box–Muller, so ticks look like returns rather than uniform noise. */
    function gaussian(rand) {
        let u = 0, v = 0;
        while (u === 0) u = rand();
        while (v === 0) v = rand();
        return Math.sqrt(-2 * Math.log(u)) * Math.cos(2 * Math.PI * v);
    }

    /**
     * Runs `step(progress)` from 0 to 1 over `duration`, and guarantees step(1) lands.
     * requestAnimationFrame is suspended in hidden or occluded tabs, so every reveal
     * animation carries a timer that jumps it to its finished state rather than
     * leaving half a chart or a stat counting up from zero forever.
     */
    function animate(duration, step) {
        if (reduceMotion || document.hidden) { step(1); return; }

        let finished = false;
        const complete = () => {
            if (finished) return;
            finished = true;
            step(1);
        };
        const guard = setTimeout(complete, duration + 1500);

        const t0 = performance.now();
        const frame = now => {
            if (finished) return;
            const p = Math.min(1, (now - t0) / duration);
            step(p);
            if (p < 1) {
                requestAnimationFrame(frame);
            } else {
                finished = true;
                clearTimeout(guard);
            }
        };
        requestAnimationFrame(frame);
    }

    /* ── boot screen ───────────────────────────────────────────────────────── */

    const BOOT_LINES = [
        ['> VKLP TERMINAL v1.0.0 — GLOBAL TALENT EXCHANGE', ''],
        ['> initializing market data feed ......... ', 'OK'],
        ['> loading security profile: VKLP ........ ', 'OK'],
        ['> mounting trade blotter (5 executions) . ', 'OK'],
        ['> building order book depth ............. ', 'OK'],
        ['> marking portfolio holdings to market .. ', 'OK'],
        ['> subscribing to news wire .............. ', 'OK'],
        ['> risk check: candidate status .......... ', 'HIREABLE'],
        ['> analyst consensus ..................... ', 'STRONG BUY'],
        ['', ''],
        ['> MARKET OPEN. VKLP IS ACCEPTING OFFERS.', '']
    ];

    const BOOT_TYPE_MS = 1200;   // how long the full sequence takes to type
    const BOOT_HOLD_MS = 300;    // beat after the last line before dismissing
    const BOOT_MAX_MS = 3000;    // hard ceiling, whatever the frame rate does

    /**
     * Set once the curtain has been raised. A returning visitor gets the terminal
     * directly — the boot sequence is a first-impression, and on the second visit it is
     * just latency. The inline script in the document head reads the same key to keep
     * the overlay from painting at all; this module owns writing it.
     */
    const BOOT_SEEN_KEY = 'vklp.booted';

    /** localStorage throws outright in some privacy modes — never let it break boot. */
    function bootSeen() {
        try { return localStorage.getItem(BOOT_SEEN_KEY) === '1'; } catch { return false; }
    }

    function markBootSeen() {
        try { localStorage.setItem(BOOT_SEEN_KEY, '1'); } catch { /* nothing to do */ }
    }

    /**
     * Types the boot log, then resolves. Progress is derived from elapsed time rather
     * than one timer per character, so a background tab or a dropped frame catches up
     * on the next paint instead of stalling half-typed. Resolves no matter what:
     * nothing downstream is allowed to wait on an animation.
     */
    function boot() {
        const el = $('#boot');
        const log = $('#boot-log');
        if (!el || !log) return Promise.resolve();

        if (bootSeen()) {
            el.remove();
            return Promise.resolve();
        }

        document.body.classList.add('booting');

        // Pre-build one row per line so the layout never jumps as text lands.
        const rows = BOOT_LINES.map(([, status]) => {
            const row = document.createElement('div');
            const text = document.createElement('span');
            const badge = document.createElement('span');
            badge.className = status === 'OK' ? 'ok' : 'warn';
            row.append(text, badge);
            log.appendChild(row);
            return { text, badge };
        });

        const total = BOOT_LINES.reduce((n, [t]) => n + t.length, 0);
        let done = false;
        let resolveBoot;

        const finish = () => {
            if (done) return;
            done = true;
            markBootSeen();
            el.classList.add('gone');
            document.body.classList.remove('booting');
            window.removeEventListener('keydown', finish);
            el.removeEventListener('click', finish);
            document.removeEventListener('visibilitychange', onHide);
            setTimeout(() => el.remove(), 520);
            resolveBoot();
        };

        // Nobody is watching an animation in a hidden tab, and hidden tabs get their
        // timers frozen — so skip straight to the terminal.
        const onHide = () => { if (document.hidden) finish(); };

        window.addEventListener('keydown', finish);
        el.addEventListener('click', finish);
        document.addEventListener('visibilitychange', onHide);

        const paint = chars => {
            let budget = chars;
            BOOT_LINES.forEach(([text, status], i) => {
                const take = Math.max(0, Math.min(text.length, budget));
                rows[i].text.textContent = text.slice(0, take);
                rows[i].badge.textContent = take >= text.length ? status : '';
                budget -= text.length;
            });
        };

        return new Promise(resolve => {
            resolveBoot = resolve;

            if (reduceMotion || document.hidden) {
                paint(total);
                setTimeout(finish, 600);
                return;
            }

            // setInterval rather than rAF: a throttled tab still gets ticks, and because
            // the character count is computed from elapsed time, one late tick catches up
            // the whole sequence instead of leaving it half-typed.
            const t0 = performance.now();
            const timer = setInterval(() => {
                if (done) { clearInterval(timer); return; }
                const elapsed = performance.now() - t0;
                paint(Math.ceil((elapsed / BOOT_TYPE_MS) * total));
                if (elapsed >= BOOT_TYPE_MS) {
                    clearInterval(timer);
                    setTimeout(finish, BOOT_HOLD_MS);
                }
            }, 32);
        });
    }

    /* ── IST clock ─────────────────────────────────────────────────────────── */

    function startClock() {
        const el = $('#clock');
        if (!el) return;
        const fmt = new Intl.DateTimeFormat('en-GB', {
            timeZone: 'Asia/Kolkata',
            hour: '2-digit', minute: '2-digit', second: '2-digit', hour12: false
        });
        const tick = () => { el.textContent = fmt.format(new Date()); };
        tick();
        setInterval(tick, 1000);
    }

    /* ── prompt line ───────────────────────────────────────────────────────── */

    const PROMPT_CMDS = [
        'whoami',
        'cat profile.json | grep status',
        'SELECT * FROM shipped WHERE impact > 0;',
        'tail -f production.log',
        './hire --ticker VKLP --side BUY'
    ];

    function typePrompt() {
        const el = $('#prompt-cmd');
        if (!el) return;
        if (reduceMotion) { el.textContent = PROMPT_CMDS[0]; return; }

        let idx = 0;
        const run = () => {
            const cmd = PROMPT_CMDS[idx % PROMPT_CMDS.length];
            let c = 0;
            const typeIn = () => {
                el.textContent = cmd.slice(0, ++c);
                if (c < cmd.length) return setTimeout(typeIn, 46);
                setTimeout(erase, 2200);
            };
            const erase = () => {
                const t = el.textContent;
                el.textContent = t.slice(0, -1);
                if (el.textContent.length) return setTimeout(erase, 18);
                idx++;
                setTimeout(run, 380);
            };
            typeIn();
        };
        run();
    }

    /* ── quote: seeded random walk, always drifting up ─────────────────────── */

    function createTape(q) {
        const rand = mulberry32(q.seed || 1);
        const floor = q.dayLow || q.prevClose;
        // The walk drifts up, but a tab left open all afternoon shouldn't print 400.
        // Drift fades to zero as the price approaches the ceiling, so it converges.
        const ceiling = q.last * 1.25;

        let price = q.last;
        let high = q.dayHigh || q.last;
        let low = q.dayLow || q.last;

        return {
            quote: q,
            get price() { return price; },
            get high() { return high; },
            get low() { return low; },
            tick() {
                const room = Math.max(0, Math.min(1, (ceiling - price) / (ceiling - q.last)));
                const ret = q.drift * room + q.volatility * gaussian(rand);
                price = price * (1 + ret);

                // VKLP does not print below its session floor — the index only trends up.
                if (price < floor) price = floor + Math.abs(gaussian(rand)) * floor * q.volatility;
                if (price > ceiling) price = ceiling - Math.abs(gaussian(rand)) * ceiling * q.volatility;

                price = Math.round(price * 100) / 100;
                if (price > high) high = price;
                if (price < low) low = price;
                return price;
            }
        };
    }

    function renderQuote(tape) {
        const price = tape.price;
        const prev = tape.quote.prevClose;
        const chg = price - prev;
        const pct = (chg / prev) * 100;
        const sign = chg >= 0 ? '+' : '−';
        const abs = Math.abs(chg).toFixed(2);
        const absPct = Math.abs(pct).toFixed(2);

        const px = $('#px');
        if (px) {
            const previous = parseFloat(px.textContent);
            px.textContent = price.toFixed(2);
            px.classList.remove('tick-up', 'tick-dn');
            if (!Number.isNaN(previous) && price !== previous) {
                px.classList.add(price > previous ? 'tick-up' : 'tick-dn');
                setTimeout(() => px.classList.remove('tick-up', 'tick-dn'), 420);
            }
        }

        const chgEl = $('#px-chg');
        if (chgEl) {
            chgEl.textContent = `${sign}${abs} (${sign}${absPct}%)`;
            chgEl.classList.toggle('dn', chg < 0);
        }

        const chartPx = $('#chart-px');
        if (chartPx) chartPx.textContent = price.toFixed(2);

        const chartChg = $('#chart-chg');
        if (chartChg) {
            chartChg.textContent = `${chg >= 0 ? '▲' : '▼'} ${absPct}%`;
            chartChg.classList.toggle('up', chg >= 0);
            chartChg.classList.toggle('dn', chg < 0);
        }

        const h = $('#hud-h'), l = $('#hud-l');
        if (h) h.textContent = tape.high.toFixed(2);
        if (l) l.textContent = tape.low.toFixed(2);

        document.title = `${price.toFixed(2)} ${sign}${absPct}% · VKLP — Vikalp Shandilya`;
    }

    /* ── career index chart ────────────────────────────────────────────────── */

    /** Career waypoints, positioned as a fraction of the 2019→now timeline. */
    const MILESTONES = [
        { at: 0.00, label: 'IPO' },
        { at: 0.46, label: 'INTERN' },
        { at: 0.53, label: 'SEP' },
        { at: 0.60, label: 'SWE' },
        { at: 0.79, label: 'KITE' },
        { at: 0.86, label: 'SWE II' }
    ];

    function buildSeries(q, n = 220) {
        const rand = mulberry32((q.seed || 1) ^ 0x9e3779b9);
        const start = q.yearLow || q.last * 0.35;
        const series = [start];
        for (let i = 1; i < n; i++) series.push(series[i - 1] * (1 + 0.02 * gaussian(rand)));

        // Re-anchor: the walk supplies the texture, the profile supplies the endpoints.
        const lo = Math.min(...series), hi = Math.max(...series);
        const span = hi - lo || 1;
        return series.map((v, i) => {
            const shaped = (v - lo) / span;                 // 0..1 texture
            const ramp = Math.pow(i / (n - 1), 0.78);       // the underlying up-trend
            const blend = ramp * 0.86 + shaped * 0.14;
            return start + (q.last - start) * blend;
        });
    }

    function createChart(canvas, q) {
        if (!canvas) return null;
        const ctx = canvas.getContext('2d');
        const series = buildSeries(q);
        let reveal = reduceMotion ? 1 : 0;
        let w = 0, h = 0;

        // Bottom padding carries two rows of milestone labels — the 2022/23 waypoints sit
        // close enough together that a single row would overlap.
        const pad = { l: 8, r: 62, t: 18, b: 38 };

        function resize() {
            const dpr = Math.min(window.devicePixelRatio || 1, 2);
            const rect = canvas.getBoundingClientRect();
            w = rect.width;
            h = rect.height || 260;
            canvas.width = Math.round(w * dpr);
            canvas.height = Math.round(h * dpr);
            ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
            draw();
        }

        function draw() {
            if (!w) return;
            const lo = Math.min(...series) * 0.96;
            const hi = Math.max(...series) * 1.04;
            const plotW = w - pad.l - pad.r;
            const plotH = h - pad.t - pad.b;
            const x = i => pad.l + (i / (series.length - 1)) * plotW;
            const y = v => pad.t + plotH - ((v - lo) / (hi - lo)) * plotH;

            ctx.clearRect(0, 0, w, h);

            // grid
            ctx.strokeStyle = 'rgba(30,52,35,.75)';
            ctx.lineWidth = 1;
            ctx.setLineDash([2, 4]);
            for (let g = 0; g <= 4; g++) {
                const gy = Math.round(pad.t + (plotH / 4) * g) + .5;
                ctx.beginPath();
                ctx.moveTo(pad.l, gy);
                ctx.lineTo(w - pad.r, gy);
                ctx.stroke();
                ctx.setLineDash([]);
                ctx.fillStyle = 'rgba(85,122,103,.85)';
                ctx.font = '9px "JetBrains Mono", monospace';
                ctx.textAlign = 'left';
                ctx.fillText((hi - ((hi - lo) / 4) * g).toFixed(0), w - pad.r + 8, gy + 3);
                ctx.setLineDash([2, 4]);
            }
            ctx.setLineDash([]);

            const cut = Math.max(2, Math.floor(series.length * reveal));

            // area fill
            const grad = ctx.createLinearGradient(0, pad.t, 0, h - pad.b);
            grad.addColorStop(0, 'rgba(0,255,157,.30)');
            grad.addColorStop(1, 'rgba(0,255,157,0)');
            ctx.beginPath();
            ctx.moveTo(x(0), h - pad.b);
            for (let i = 0; i < cut; i++) ctx.lineTo(x(i), y(series[i]));
            ctx.lineTo(x(cut - 1), h - pad.b);
            ctx.closePath();
            ctx.fillStyle = grad;
            ctx.fill();

            // line
            ctx.beginPath();
            for (let i = 0; i < cut; i++) {
                const px = x(i), py = y(series[i]);
                i === 0 ? ctx.moveTo(px, py) : ctx.lineTo(px, py);
            }
            ctx.strokeStyle = '#00ff9d';
            ctx.lineWidth = 1.8;
            ctx.lineJoin = 'round';
            ctx.shadowColor = 'rgba(0,255,157,.85)';
            ctx.shadowBlur = 9;
            ctx.stroke();
            ctx.shadowBlur = 0;

            // milestones — labels alternate between two rows so neighbouring years clear
            ctx.font = '8.5px "JetBrains Mono", monospace';
            const showLabels = plotW > 380;
            MILESTONES.forEach((m, n) => {
                const i = Math.round(m.at * (series.length - 1));
                if (i >= cut) return;
                const mx = x(i), my = y(series[i]);
                const row = n % 2 === 0 ? 13 : 25;

                ctx.setLineDash([2, 3]);
                ctx.strokeStyle = 'rgba(255,176,0,.35)';
                ctx.lineWidth = 1;
                ctx.beginPath();
                ctx.moveTo(mx, my);
                ctx.lineTo(mx, h - pad.b + row - 9);
                ctx.stroke();
                ctx.setLineDash([]);

                ctx.fillStyle = '#ffb000';
                ctx.beginPath();
                ctx.arc(mx, my, 2.6, 0, Math.PI * 2);
                ctx.fill();

                if (!showLabels) return;
                // Keep the first and last labels inside the plot rather than clipped.
                const half = ctx.measureText(m.label).width / 2;
                ctx.textAlign = 'center';
                const lx = Math.min(Math.max(mx, pad.l + half), w - pad.r - half);
                ctx.fillText(m.label, lx, h - pad.b + row);
            });

            // last print
            const li = cut - 1;
            const lx = x(li), ly = y(series[li]);
            ctx.setLineDash([3, 4]);
            ctx.strokeStyle = 'rgba(0,255,157,.4)';
            ctx.beginPath();
            ctx.moveTo(lx, ly);
            ctx.lineTo(w - pad.r, ly);
            ctx.stroke();
            ctx.setLineDash([]);

            ctx.fillStyle = '#00ff9d';
            ctx.shadowColor = 'rgba(0,255,157,.9)';
            ctx.shadowBlur = 10;
            ctx.beginPath();
            ctx.arc(lx, ly, 3.4, 0, Math.PI * 2);
            ctx.fill();
            ctx.shadowBlur = 0;

            const label = series[li].toFixed(2);
            ctx.font = '700 10px "JetBrains Mono", monospace';
            const tw = ctx.measureText(label).width + 10;
            ctx.fillStyle = '#00ff9d';
            ctx.fillRect(w - pad.r + 4, ly - 8, tw, 16);
            ctx.fillStyle = '#04140c';
            ctx.textAlign = 'center';
            ctx.fillText(label, w - pad.r + 4 + tw / 2, ly + 3.5);

            // all-time-high marker
            ctx.textAlign = 'left';
            ctx.font = '8.5px "JetBrains Mono", monospace';
            ctx.fillStyle = 'rgba(255,176,0,.85)';
            ctx.fillText('ALL-TIME HIGH', pad.l + 2, pad.t - 6);
        }

        function animateIn() {
            animate(1500, p => {
                reveal = 1 - Math.pow(1 - p, 3);
                draw();
            });
        }

        if (window.ResizeObserver) new ResizeObserver(resize).observe(canvas);
        window.addEventListener('resize', resize);
        resize();

        return {
            animateIn,
            push(price) {
                series[series.length - 1] = price;
                if (reveal >= 1) draw();
            }
        };
    }

    /* ── stat count-up ─────────────────────────────────────────────────────── */

    function countUp() {
        const stats = $$('.stat');
        if (!stats.length) return;

        const run = stat => {
            const target = parseFloat(stat.dataset.value);
            const decimals = parseInt(stat.dataset.decimals || '0', 10);
            const prefix = stat.dataset.prefix || '';
            const suffix = stat.dataset.suffix || '';
            const out = $('.stat-v', stat);
            if (!out || Number.isNaN(target)) return;

            animate(1250, p => {
                const eased = 1 - Math.pow(1 - p, 3);
                const value = p >= 1 ? target : target * eased;
                out.textContent = prefix + value.toFixed(decimals) + suffix;
            });
        };

        if (!('IntersectionObserver' in window)) { stats.forEach(run); return; }
        const io = new IntersectionObserver((entries, obs) => {
            entries.forEach(e => {
                if (!e.isIntersecting) return;
                run(e.target);
                obs.unobserve(e.target);
            });
        }, { threshold: .35 });
        stats.forEach(s => io.observe(s));
    }

    /* ── holdings: capture frames ──────────────────────────────────────────── */

    /**
     * A holding declares a screenshot before the file necessarily exists. Rather than
     * printing a broken-image icon on a page that is otherwise all deliberate glyphs,
     * swap a missing capture for the "no feed" placeholder already in the markup.
     */
    function wireHoldingShots() {
        $$('.hold-shot img').forEach(img => {
            const frame = img.closest('.hold-shot');
            const pending = frame ? $('.shot-pending', frame) : null;
            if (!pending) return;

            const fail = () => {
                img.hidden = true;
                pending.hidden = false;
            };
            img.addEventListener('error', fail);
            // Covers an image that already failed before this script ran.
            if (img.complete && img.naturalWidth === 0) fail();
        });
    }

    /* ── navigation: keys 0–6 + function bar highlighting ──────────────────── */

    function jumpTo(n) {
        const panel = document.getElementById('p' + n);
        if (!panel) return;
        panel.scrollIntoView({ behavior: reduceMotion ? 'auto' : 'smooth', block: 'start' });
        // Move focus with the viewport. Without this a keyboard user lands on a panel
        // visually while their focus stays wherever it was, so the next Tab throws them
        // back — the exact problem a skip link exists to solve. preventScroll because
        // scrollIntoView above already owns the scrolling (and animates it).
        panel.focus({ preventScroll: true });
        panel.classList.remove('flash');
        void panel.offsetWidth;
        panel.classList.add('flash');
        setTimeout(() => panel.classList.remove('flash'), 800);
    }

    function setupNav() {
        $$('.fbar a[data-jump]').forEach(a => {
            a.addEventListener('click', e => {
                e.preventDefault();
                jumpTo(a.dataset.jump);
            });
        });

        const links = new Map($$('.fbar a[data-jump]').map(a => [a.dataset.jump, a]));
        if ('IntersectionObserver' in window) {
            const io = new IntersectionObserver(entries => {
                entries.forEach(e => {
                    if (!e.isIntersecting) return;
                    links.forEach(a => a.classList.remove('active'));
                    const a = links.get(e.target.dataset.panel);
                    if (a) a.classList.add('active');
                });
            }, { rootMargin: '-45% 0px -50% 0px' });
            $$('.panel').forEach(p => io.observe(p));
        }
    }

    /* ── command palette ───────────────────────────────────────────────────── */

    function setupPalette() {
        const dlg = $('#palette');
        const input = $('#pal-input');
        const list = $('#pal-list');
        const openBtn = $('#palette-open');
        if (!dlg || !input || !list) return;

        // Read targets out of the DOM so there is exactly one copy of each URL.
        const href = sel => { const el = $(sel); return el ? el.getAttribute('href') : null; };

        const COMMANDS = [
            { cmd: 'hire', desc: 'Place a buy order — email me', run: () => go(href('.cta')) },
            { cmd: 'resume', desc: 'Download the prospectus (PDF resume)', run: () => go(href('.cta-ghost')) },
            { cmd: 'github', desc: 'Open GitHub profile', run: () => go(href('.exec-cell[href*="github"]'), true) },
            { cmd: 'linkedin', desc: 'Open LinkedIn profile', run: () => go(href('.exec-cell[href*="linkedin"]'), true) },
            { cmd: 'email', desc: 'Compose an email', run: () => go(href('.exec-cell[href^="mailto"]')) },
            { cmd: 'top', desc: 'Back to the top of the terminal', run: () => window.scrollTo({ top: 0, behavior: reduceMotion ? 'auto' : 'smooth' }) },
            { cmd: 'profile', desc: 'Jump to [0] Security Profile', run: () => jumpTo(0) },
            { cmd: 'index', desc: 'Jump to [1] Career Index', run: () => jumpTo(1) },
            { cmd: 'blotter', desc: 'Jump to [2] Trade Blotter', run: () => jumpTo(2) },
            { cmd: 'book', desc: 'Jump to [3] Order Book', run: () => jumpTo(3) },
            { cmd: 'position', desc: 'Jump to [4] Concentrated Position — the KITE holding', run: () => jumpTo(4) },
            { cmd: 'news', desc: 'Jump to [5] News Wire', run: () => jumpTo(5) },
            { cmd: 'print', desc: 'Print / save this terminal as PDF', run: () => window.print() }
        ];

        function go(url, blank) {
            if (!url) return;
            if (blank) window.open(url, '_blank', 'noopener');
            else window.location.href = url;
        }

        let filtered = COMMANDS.slice();
        let cursor = 0;

        function render() {
            list.innerHTML = '';
            if (!filtered.length) {
                const li = document.createElement('li');
                li.className = 'pal-empty';
                li.textContent = 'no matching command';
                list.appendChild(li);
                return;
            }
            filtered.forEach((c, i) => {
                const li = document.createElement('li');
                li.setAttribute('role', 'option');
                li.setAttribute('aria-selected', String(i === cursor));
                li.innerHTML = `<span class="pc"></span><span class="pd"></span>`;
                $('.pc', li).textContent = c.cmd;
                $('.pd', li).textContent = c.desc;
                li.addEventListener('mouseenter', () => { cursor = i; render(); });
                li.addEventListener('click', () => exec());
                list.appendChild(li);
            });
        }

        function filter(q) {
            const s = q.trim().toLowerCase();
            filtered = s ? COMMANDS.filter(c => c.cmd.includes(s) || c.desc.toLowerCase().includes(s)) : COMMANDS.slice();
            cursor = 0;
            render();
        }

        function exec() {
            const c = filtered[cursor];
            close();
            if (c) c.run();
        }

        function open() {
            input.value = '';
            filter('');
            if (typeof dlg.showModal === 'function') dlg.showModal();
            else dlg.setAttribute('open', '');
            input.focus();
        }

        function close() {
            if (typeof dlg.close === 'function' && dlg.open) dlg.close();
            else dlg.removeAttribute('open');
        }

        input.addEventListener('input', () => filter(input.value));
        input.addEventListener('keydown', e => {
            if (e.key === 'ArrowDown') { e.preventDefault(); cursor = Math.min(cursor + 1, filtered.length - 1); render(); }
            else if (e.key === 'ArrowUp') { e.preventDefault(); cursor = Math.max(cursor - 1, 0); render(); }
            else if (e.key === 'Enter') { e.preventDefault(); exec(); }
        });
        if (openBtn) openBtn.addEventListener('click', open);
        dlg.addEventListener('click', e => { if (e.target === dlg) close(); });

        window.addEventListener('keydown', e => {
            if (e.metaKey || e.ctrlKey || e.altKey) return;
            const tag = (e.target.tagName || '').toLowerCase();
            if (tag === 'input' || tag === 'textarea') return;

            if (e.key === '/') { e.preventDefault(); open(); return; }
            if (/^[0-6]$/.test(e.key)) { e.preventDefault(); jumpTo(e.key); }
        });
    }

    /* ── wiring ────────────────────────────────────────────────────────────── */

    /** Fall back to the quote baked into the markup when there is no server (file://). */
    function quoteFromDom() {
        const el = $('#hdr-quote');
        const d = el ? el.dataset : {};
        return {
            last: parseFloat(d.last) || 120,
            prevClose: parseFloat(d.prev) || 111.7,
            dayHigh: parseFloat(d.last) || 120,
            dayLow: parseFloat(d.prev) || 111.7,
            yearLow: 40,
            seed: parseInt(d.seed, 10) || 120500,
            drift: parseFloat(d.drift) || 0.00042,
            volatility: parseFloat(d.vol) || 0.0021
        };
    }

    async function loadQuote() {
        try {
            const res = await fetch('./data/quote.json', { cache: 'no-cache' });
            if (!res.ok) throw new Error(res.status);
            const q = await res.json();
            return { ...quoteFromDom(), ...q };
        } catch {
            return quoteFromDom();
        }
    }

    const sleep = ms => new Promise(r => setTimeout(r, ms));

    async function main() {
        startClock();
        setupNav();
        setupPalette();
        wireHoldingShots();

        // The boot overlay is decoration. Start it, but build the terminal underneath it
        // straight away so a stalled animation can never leave the page half-alive.
        const booted = boot();

        const q = await loadQuote();
        const tape = createTape(q);
        const chart = createChart($('#chart'), q);
        renderQuote(tape);

        setInterval(() => {
            const price = tape.tick();
            renderQuote(tape);
            if (chart) chart.push(price);
        }, 1400);

        // Hold the reveal animations until the curtain lifts — but never indefinitely.
        await Promise.race([booted, sleep(BOOT_MAX_MS)]);

        typePrompt();
        countUp();
        if (chart) chart.animateIn();
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', main);
    else main();
})();
