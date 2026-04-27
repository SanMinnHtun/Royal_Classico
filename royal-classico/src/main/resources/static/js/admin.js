/**
 * Royal Classico FC — Admin Client SDK
 * ─────────────────────────────────────────────────────────────
 * "Security through Obscurity" — there is no visible login page.
 *
 * HOW TO ACTIVATE (paste into browser console on localhost):
 *   localStorage.setItem('admin_secret_token', 'RC-ADMIN-XKDF92-CLASSIFIED');
 *   location.reload();
 *
 * HOW TO DEACTIVATE:
 *   localStorage.removeItem('admin_secret_token');
 *   location.reload();
 *
 * Once active, all fetch() calls to /api/v1/rc-management-internal/*
 * automatically carry the X-Admin-Secret header, and admin UI elements
 * (data-admin-only) become visible.
 * ─────────────────────────────────────────────────────────────
 */

(function () {
  'use strict';

  /* ── 1. Read token from localStorage ─────────────────────── */
  const STORAGE_KEY  = 'admin_secret_token';
  const ADMIN_HEADER = 'X-Admin-Secret';
  const ADMIN_API_BASE = '/api/v1/rc-management-internal';

  const token = localStorage.getItem(STORAGE_KEY);
  const isAdmin = !!token;

  /* Expose globally so inline handlers can reference it */
  window.RC = window.RC || {};
  window.RC.isAdmin = isAdmin;
  window.RC.token   = token;

  /* ── 2. Show / hide admin-only UI ────────────────────────── */
  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-admin-only]').forEach(function (el) {
      el.style.display = isAdmin ? (el.dataset.adminDisplay || 'flex') : 'none';
    });

    if (isAdmin) {
      /* Inject a subtle "Admin Mode" indicator in the navbar */
      var navbar = document.querySelector('.navbar__inner');
      if (navbar) {
        var badge = document.createElement('span');
        badge.id = 'admin-badge';
        badge.title = 'Admin Mode Active — click to deactivate';
        badge.textContent = '⚙ ADMIN';
        badge.style.cssText = [
          'font-size:0.65rem', 'font-weight:700', 'letter-spacing:0.12em',
          'color:#0A1128', 'background:#C5A065', 'padding:3px 10px',
          'border-radius:4px', 'cursor:pointer', 'font-family:Montserrat,sans-serif',
          'text-transform:uppercase', 'user-select:none'
        ].join(';');
        badge.addEventListener('click', function () {
          if (confirm('Deactivate admin mode?')) {
            localStorage.removeItem(STORAGE_KEY);
            location.reload();
          }
        });
        navbar.appendChild(badge);
      }
    }
  });

  /* ── 3. Intercept fetch() — inject X-Admin-Secret ────────── */
  if (isAdmin) {
    var _originalFetch = window.fetch;

    window.fetch = function (input, init) {
      var url = (typeof input === 'string') ? input : input.url;

      /* Only inject header for requests to our admin API */
      if (typeof url === 'string' && url.includes(ADMIN_API_BASE)) {
        init = init || {};
        init.headers = Object.assign({}, init.headers || {});
        init.headers[ADMIN_HEADER] = token;
      }

      return _originalFetch.call(this, input, init);
    };
  }

  /* ── 4. Convenience helpers exposed on window.RC ─────────── */

  /**
   * RC.api(method, path, body)
   * Wrapper for admin API calls — always includes X-Admin-Secret.
   * Path is relative to ADMIN_API_BASE, e.g. '/players', '/news/abc123'
   *
   * Example:
   *   RC.api('DELETE', '/players/abc123')
   *   RC.api('POST',   '/news', formData)
   */
  window.RC.api = function (method, path, body) {
    if (!isAdmin) {
      return Promise.reject(new Error('Admin token not set. Run: localStorage.setItem("admin_secret_token","<your-secret>")'));
    }
    var headers = {};
    headers[ADMIN_HEADER] = token;
    /* Don't set Content-Type for FormData — browser sets it with boundary */
    if (!(body instanceof FormData)) {
      headers['Content-Type'] = 'application/json';
    }
    return fetch(ADMIN_API_BASE + path, {
      method:  method,
      headers: headers,
      body:    body instanceof FormData ? body : (body ? JSON.stringify(body) : undefined)
    }).then(function (res) {
      if (!res.ok) throw new Error('API error: ' + res.status + ' ' + res.statusText);
      /* Return parsed JSON or null for 204 No Content */
      return res.status === 204 ? null : res.json().catch(function () { return null; });
    });
  };

  /**
   * Quick console helpers — printed to console only in admin mode.
   */
  if (isAdmin) {
    console.groupCollapsed('%c Royal Classico — Admin SDK Ready ', 'background:#003366;color:#C5A065;font-weight:bold;padding:2px 6px;border-radius:3px;');
    console.log('%cAvailable helpers:', 'color:#C5A065;font-weight:bold;');
    console.log('  RC.api("GET",    "/players")');
    console.log('  RC.api("DELETE", "/players/<id>")');
    console.log('  RC.api("DELETE", "/news/<id>")');
    console.log('  RC.api("POST",   "/news", formData)');
    console.log('%cDeactivate:', 'color:#C5A065;font-weight:bold;');
    console.log('  localStorage.removeItem("admin_secret_token"); location.reload();');
    console.groupEnd();
  }

})();
